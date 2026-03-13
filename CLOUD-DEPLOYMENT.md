# Cloud-Ready CRM Application - Deployment Guide

## Overview

This CRM application has been modernized for cloud deployment with the following cloud-native features:

### Cloud Readiness Features

1. **RESTful API Design**
   - All controllers support REST API endpoints
   - Compatible with cloud load balancers and API gateways
   - Stateless request handling for horizontal scaling

2. **Distributed Session Management**
   - Redis-based session storage
   - No server affinity required
   - Supports horizontal scaling across multiple instances

3. **Distributed Caching**
   - Redis cache for improved performance
   - Shared cache across application instances
   - Configurable cache TTL and eviction policies

4. **Immutable Infrastructure**
   - No local file system writes
   - PDF generation in memory
   - Cloud storage integration ready (AWS S3)

5. **Environment-Based Configuration**
   - All configuration externalized via environment variables
   - 12-factor app compliant
   - Separate profiles for local, dev, and cloud environments

6. **Connection Pooling**
   - HikariCP for efficient database connections
   - Configurable pool sizes and timeouts
   - Optimized for cloud database services (AWS RDS)

7. **Health Checks & Monitoring**
   - Spring Boot Actuator endpoints
   - Prometheus metrics export
   - Custom health indicators
   - CloudWatch compatible logging

8. **Container Ready**
   - Multi-stage Dockerfile for optimized images
   - Non-root user for security
   - Health checks for orchestration
   - Graceful shutdown support

## AWS Deployment

### Prerequisites

- AWS Account with appropriate permissions
- AWS CLI configured
- Docker installed
- Maven 3.6+
- Java 11+

### Step 1: Build the Application

```bash
# Build JAR file
mvn clean package -DskipTests

# Build Docker image
docker build -t crm-application:latest .

# Tag for ECR
docker tag crm-application:latest <account-id>.dkr.ecr.<region>.amazonaws.com/crm:latest
```

### Step 2: Push to AWS ECR

```bash
# Login to ECR
aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com

# Create repository (if not exists)
aws ecr create-repository --repository-name crm --region <region>

# Push image
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/crm:latest
```

### Step 3: Create AWS Resources

#### Create RDS MySQL Database

```bash
aws rds create-db-instance \
  --db-instance-identifier crm-database \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password <password> \
  --allocated-storage 20 \
  --vpc-security-group-ids <security-group-id> \
  --db-subnet-group-name <subnet-group> \
  --backup-retention-period 7 \
  --region <region>
```

#### Create ElastiCache Redis Cluster

```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id crm-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --cache-subnet-group-name <subnet-group> \
  --security-group-ids <security-group-id> \
  --region <region>
```

#### Create S3 Bucket for PDF Storage

```bash
aws s3 mb s3://crm-pdfs --region <region>
```

### Step 4: Deploy to ECS

```bash
# Deploy using CloudFormation
aws cloudformation create-stack \
  --stack-name crm-application \
  --template-body file://aws-deployment.yml \
  --parameters \
    ParameterKey=VpcId,ParameterValue=<vpc-id> \
    ParameterKey=SubnetIds,ParameterValue=<subnet-1>,<subnet-2> \
    ParameterKey=DatabaseEndpoint,ParameterValue=<rds-endpoint> \
    ParameterKey=DatabaseUsername,ParameterValue=admin \
    ParameterKey=DatabasePassword,ParameterValue=<password> \
    ParameterKey=RedisEndpoint,ParameterValue=<redis-endpoint> \
    ParameterKey=ContainerImage,ParameterValue=<ecr-image-uri> \
  --capabilities CAPABILITY_IAM \
  --region <region>
```

### Step 5: Configure Environment Variables

The application uses the following environment variables:

```bash
# Database Configuration
DATABASE_URL=jdbc:mysql://<rds-endpoint>:3306/crm?useSSL=true
DATABASE_USERNAME=admin
DATABASE_PASSWORD=<password>
DB_DDL_AUTO=update

# Redis Configuration
REDIS_HOST=<redis-endpoint>
REDIS_PORT=6379
REDIS_PASSWORD=<password>

# Cache Configuration
cache.enabled=true

# Session Configuration
SESSION_STORE_TYPE=redis

# Cloud Storage
cloud.storage.enabled=true
cloud.storage.bucket=crm-pdfs
cloud.storage.region=us-east-1

# Application Configuration
SPRING_PROFILES_ACTIVE=cloud
SERVER_PORT=8080
```

## Local Development with Docker Compose

For local development that simulates cloud environment:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f crm-app

# Stop services
docker-compose down
```

## Monitoring and Observability

### Health Check Endpoints

- Application Health: `http://<host>:8080/appinfo/health`
- Metrics: `http://<host>:8080/appinfo/metrics`
- Prometheus: `http://<host>:8080/appinfo/prometheus`

### CloudWatch Logs

Logs are automatically sent to CloudWatch Logs group: `/ecs/crm-application`

### Metrics

Application metrics are exported in Prometheus format and can be scraped by:
- AWS CloudWatch Container Insights
- Prometheus server
- Grafana dashboards

## Scaling

### Horizontal Scaling

The application supports horizontal scaling:

```bash
# Scale ECS service
aws ecs update-service \
  --cluster crm-cluster \
  --service crm-service \
  --desired-count 5 \
  --region <region>
```

### Auto Scaling

Auto scaling is configured based on CPU utilization (70% target):
- Minimum instances: 2
- Maximum instances: 10
- Scale-out cooldown: 60 seconds
- Scale-in cooldown: 300 seconds

## Security Best Practices

1. **Use AWS Secrets Manager** for sensitive credentials
2. **Enable SSL/TLS** for database connections
3. **Use VPC** for network isolation
4. **Enable encryption** for RDS and ElastiCache
5. **Use IAM roles** instead of access keys
6. **Enable CloudTrail** for audit logging
7. **Use Security Groups** for network access control

## Troubleshooting

### Application Won't Start

Check CloudWatch logs:
```bash
aws logs tail /ecs/crm-application --follow --region <region>
```

### Database Connection Issues

Verify security groups allow traffic from ECS tasks to RDS.

### Redis Connection Issues

Verify ElastiCache is in the same VPC and security groups allow access.

### High Memory Usage

Adjust JVM options in Dockerfile:
```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
```

## Cost Optimization

1. Use **Fargate Spot** for non-production environments
2. Enable **RDS Auto Scaling** for storage
3. Use **ElastiCache reserved nodes** for production
4. Enable **S3 lifecycle policies** for old PDFs
5. Use **CloudWatch Logs retention** policies

## Support

For issues or questions, contact the development team.
