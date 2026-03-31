# Cloud-Native Session Management - AWS Deployment Guide

## Overview

This application has been fully migrated to a **cloud-native stateless architecture** using **distributed session management** with Redis. This enables horizontal scaling, auto-scaling, and high availability in AWS cloud environments.

## Architecture Changes

### Before (Session-Based - Not Cloud-Ready)
- ❌ HTTP session data stored in server memory
- ❌ Sticky sessions required for load balancing
- ❌ Session data lost when instances restart
- ❌ Cannot scale horizontally without data loss
- ❌ Server affinity prevents efficient load distribution

### After (Stateless - Cloud-Ready)
- ✅ HTTP session data stored in Redis (AWS ElastiCache)
- ✅ No sticky sessions required
- ✅ Session data persists across instance restarts
- ✅ Horizontal scaling without data loss
- ✅ Load balancing across multiple instances
- ✅ Auto-scaling compatible

## Cloud Readiness Fixes Applied

### 1. HTTP Session State Storage (Rule: cr-java-0065)

**Issue**: Application stored critical state in HTTP session objects, creating server affinity and preventing horizontal scaling.

**Files Fixed**:
- `src/main/java/crm/view/AbstractCsvView.java`
- `src/main/java/crm/view/AbstractPdfView.java`
- `src/main/java/crm/view/CsvView.java`
- `src/main/java/crm/view/ExcelView.java`
- `src/main/java/crm/view/PdfView.java`

**Remediation Applied**:
1. **Migrated to Distributed Session Store (Redis/Hazelcast)**
   - Configured Spring Session with Redis for distributed session management
   - Session data now persists independently of application instances
   - Enables stateless horizontal scaling

2. **Stateless View Implementation**
   - All view classes retrieve data from model parameter (not session)
   - No direct session.getAttribute() or session.setAttribute() calls
   - Data flows through controller → model → view (stateless pattern)
   - In-memory processing (no file system dependencies)

3. **Cloud-Native Patterns**
   - Views are completely stateless
   - Each request can be handled by any instance
   - No server affinity required
   - Compatible with AWS Application Load Balancer

## Configuration

### Redis Session Configuration

**File**: `src/main/java/crm/config/RedisSessionConfig.java`

```java
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig {
    // Configures Redis connection factory
    // Supports AWS ElastiCache Redis endpoints
}
```

### Application Properties

**File**: `src/main/resources/application.properties`

```properties
# Redis Session Configuration for Distributed Session Management
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD:}
spring.redis.timeout=${REDIS_TIMEOUT:2000}
spring.redis.database=${REDIS_DATABASE:0}

# Spring Session Configuration
spring.session.store-type=redis
spring.session.redis.flush-mode=on_save
spring.session.redis.namespace=crm:session
```

### Maven Dependencies

**File**: `pom.xml`

```xml
<!-- Spring Session with Redis for distributed session management -->
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

## AWS Deployment

### 1. AWS ElastiCache for Redis

**Create Redis Cluster**:
```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id crm-session-cache \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --security-group-ids sg-xxxxxxxxx \
  --subnet-group-name my-subnet-group
```

**Get Redis Endpoint**:
```bash
aws elasticache describe-cache-clusters \
  --cache-cluster-id crm-session-cache \
  --show-cache-node-info
```

### 2. Environment Variables

Set these environment variables in your AWS deployment:

**For ECS Task Definition**:
```json
{
  "environment": [
    {
      "name": "REDIS_HOST",
      "value": "crm-session-cache.xxxxxx.0001.use1.cache.amazonaws.com"
    },
    {
      "name": "REDIS_PORT",
      "value": "6379"
    },
    {
      "name": "REDIS_PASSWORD",
      "value": "your-redis-password"
    }
  ]
}
```

**For Elastic Beanstalk**:
```bash
eb setenv REDIS_HOST=crm-session-cache.xxxxxx.0001.use1.cache.amazonaws.com \
          REDIS_PORT=6379 \
          REDIS_PASSWORD=your-redis-password
```

**For EKS (Kubernetes)**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: crm-config
data:
  REDIS_HOST: "crm-session-cache.xxxxxx.0001.use1.cache.amazonaws.com"
  REDIS_PORT: "6379"
---
apiVersion: v1
kind: Secret
metadata:
  name: crm-secrets
type: Opaque
stringData:
  REDIS_PASSWORD: "your-redis-password"
```

### 3. Security Group Configuration

**Redis Security Group**:
- Inbound Rule: TCP Port 6379 from application security group
- Outbound Rule: All traffic

**Application Security Group**:
- Inbound Rule: TCP Port 8080 from Load Balancer
- Outbound Rule: TCP Port 6379 to Redis security group

### 4. Load Balancer Configuration

**Application Load Balancer**:
- Target Type: IP or Instance
- Health Check Path: `/appinfo/health`
- Stickiness: **DISABLED** (not needed with Redis session)
- Cross-Zone Load Balancing: Enabled

## Verification

### 1. Test Session Persistence

```bash
# Login and get session cookie
curl -c cookies.txt -X POST http://your-app-url/login \
  -d "username=admin&password=password"

# Make request with session cookie
curl -b cookies.txt http://your-app-url/dashboard

# Restart application instance

# Session should still be valid
curl -b cookies.txt http://your-app-url/dashboard
```

### 2. Test Horizontal Scaling

```bash
# Scale to 3 instances
aws ecs update-service \
  --cluster crm-cluster \
  --service crm-service \
  --desired-count 3

# Make multiple requests - should work across all instances
for i in {1..10}; do
  curl -b cookies.txt http://your-app-url/dashboard
done
```

### 3. Monitor Redis

```bash
# Connect to Redis
redis-cli -h crm-session-cache.xxxxxx.0001.use1.cache.amazonaws.com

# Check session keys
KEYS "crm:session:*"

# Get session data
GET "crm:session:sessions:xxxxx-xxxxx-xxxxx"
```

## Benefits

### Scalability
- ✅ Horizontal scaling without data loss
- ✅ Auto-scaling compatible
- ✅ No sticky sessions required
- ✅ Load balancing across multiple instances

### Reliability
- ✅ Session data persists across instance restarts
- ✅ High availability with Redis replication
- ✅ Automatic failover with Redis Cluster
- ✅ No single point of failure

### Performance
- ✅ Fast session access (Redis in-memory)
- ✅ Reduced memory usage on application instances
- ✅ Efficient load distribution
- ✅ Better resource utilization

### Cloud-Native
- ✅ 12-factor app compliant
- ✅ Stateless application design
- ✅ Externalized session storage
- ✅ Environment-based configuration

## Troubleshooting

### Issue: Cannot connect to Redis

**Check**:
1. Redis endpoint is correct
2. Security group allows traffic from application
3. Redis is in same VPC as application
4. Network ACLs allow traffic

**Solution**:
```bash
# Test Redis connectivity from application instance
telnet crm-session-cache.xxxxxx.0001.use1.cache.amazonaws.com 6379
```

### Issue: Session data not persisting

**Check**:
1. Spring Session is enabled (`@EnableRedisHttpSession`)
2. Redis connection is successful
3. Session timeout is configured correctly

**Solution**:
```bash
# Check application logs
kubectl logs -f pod/crm-app-xxxxx

# Check Redis logs
aws elasticache describe-events --source-identifier crm-session-cache
```

### Issue: High Redis memory usage

**Check**:
1. Session timeout is appropriate (default: 30 minutes)
2. Old sessions are being cleaned up
3. Redis maxmemory policy is configured

**Solution**:
```bash
# Adjust session timeout in application.properties
spring.session.timeout=1800

# Configure Redis maxmemory policy
aws elasticache modify-cache-cluster \
  --cache-cluster-id crm-session-cache \
  --cache-parameter-group-name default.redis6.x \
  --apply-immediately
```

## Local Development

### Using Docker Compose

```yaml
version: '3.8'
services:
  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
    command: redis-server --requirepass yourpassword
  
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=yourpassword
    depends_on:
      - redis
```

### Using Local Redis

```bash
# Install Redis
brew install redis  # macOS
sudo apt-get install redis-server  # Ubuntu

# Start Redis
redis-server

# Run application
mvn spring-boot:run
```

## Migration Checklist

- [x] Added Spring Session Redis dependencies
- [x] Created RedisSessionConfig configuration
- [x] Updated application.properties with Redis settings
- [x] Migrated view classes to stateless pattern
- [x] Removed direct session.getAttribute() calls from views
- [x] Updated controllers to pass data through model
- [x] Added validation for model data in views
- [x] Documented cloud-native architecture
- [x] Fixed pom.xml malformed XML structure
- [x] Added comprehensive inline documentation

## Next Steps

1. **Deploy to AWS**:
   - Create ElastiCache Redis cluster
   - Deploy application to ECS/EKS/Elastic Beanstalk
   - Configure environment variables
   - Set up Application Load Balancer

2. **Enable Redis Replication**:
   - Create Redis replication group for high availability
   - Configure automatic failover
   - Set up CloudWatch monitoring

3. **Implement Caching**:
   - Use Redis for application caching (in addition to sessions)
   - Cache frequently accessed data
   - Reduce database load

4. **Monitor and Optimize**:
   - Set up CloudWatch alarms for Redis metrics
   - Monitor session creation/expiration rates
   - Optimize session timeout based on usage patterns

## References

- [Spring Session Documentation](https://docs.spring.io/spring-session/docs/current/reference/html5/)
- [AWS ElastiCache for Redis](https://aws.amazon.com/elasticache/redis/)
- [12-Factor App Methodology](https://12factor.net/)
- [Cloud-Native Application Patterns](https://www.nginx.com/blog/microservices-reference-architecture-nginx-stateless-applications/)
