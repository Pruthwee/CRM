# Cloud Deployment Guide - CRM Application

## Overview
This application has been refactored to be cloud-ready with distributed session management using Redis. It follows cloud-native principles and is suitable for deployment on AWS, Azure, or GCP.

## Cloud-Ready Features

### 1. Distributed Session Management
- **Technology**: Spring Session with Redis
- **Benefits**: 
  - Stateless application instances
  - Horizontal scaling without sticky sessions
  - Session persistence across instance restarts
  - Load balancing compatibility

### 2. Environment-Based Configuration
All configuration values use environment variables with sensible defaults:

#### Database Configuration
- `DB_URL`: Database connection URL (default: jdbc:mysql://localhost:3306/crm?useSSL=false)
- `DB_USERNAME`: Database username (default: root)
- `DB_PASSWORD`: Database password (default: password)
- `DB_DDL_AUTO`: Hibernate DDL mode (default: update)
- `DB_POOL_SIZE`: Maximum connection pool size (default: 10)
- `DB_POOL_MIN_IDLE`: Minimum idle connections (default: 5)
- `DB_CONNECTION_TIMEOUT`: Connection timeout in ms (default: 30000)

#### Redis Configuration
- `REDIS_HOST`: Redis server hostname (default: localhost)
- `REDIS_PORT`: Redis server port (default: 6379)
- `REDIS_PASSWORD`: Redis password (optional)
- `SESSION_STORE_TYPE`: Session store type (default: redis)
- `REDIS_POOL_MAX_ACTIVE`: Maximum active connections (default: 8)

#### Application Configuration
- `SESSION_TIMEOUT`: Session timeout in seconds (default: 1800)
- `LOG_LEVEL`: Logging level (default: INFO)
- `THYMELEAF_CACHE`: Enable Thymeleaf caching (default: false)

### 3. Stateless View Components
All view classes (CSV, Excel, PDF) have been refactored to:
- Use only request-scoped data (no session state)
- Generate content in-memory (no file system dependency)
- Stream output directly to response
- Support concurrent requests safely

## AWS Deployment

### Prerequisites
1. AWS Account with appropriate permissions
2. RDS MySQL instance
3. ElastiCache Redis cluster
4. Elastic Beanstalk or ECS/EKS for application hosting

### Step 1: Set Up RDS MySQL
```bash
# Create RDS MySQL instance
aws rds create-db-instance \
  --db-instance-identifier crm-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password <your-password> \
  --allocated-storage 20
```

### Step 2: Set Up ElastiCache Redis
```bash
# Create ElastiCache Redis cluster
aws elasticache create-cache-cluster \
  --cache-cluster-id crm-session-cache \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1
```

### Step 3: Configure Environment Variables
Set the following environment variables in your deployment environment:

```bash
# Database Configuration
export DB_URL=jdbc:mysql://<rds-endpoint>:3306/crm?useSSL=true
export DB_USERNAME=admin
export DB_PASSWORD=<your-db-password>
export DB_DDL_AUTO=update

# Redis Configuration
export REDIS_HOST=<elasticache-endpoint>
export REDIS_PORT=6379
export SESSION_STORE_TYPE=redis

# Application Configuration
export SESSION_TIMEOUT=1800
export LOG_LEVEL=INFO
```

### Step 4: Deploy to Elastic Beanstalk
```bash
# Initialize Elastic Beanstalk
eb init -p java-8 crm-application

# Create environment
eb create crm-prod-env

# Deploy application
eb deploy
```

### Step 5: Configure Security Groups
Ensure the following security group rules:
- Application instances can connect to RDS (port 3306)
- Application instances can connect to ElastiCache (port 6379)
- Load balancer can connect to application instances (port 8080)

## Azure Deployment

### Prerequisites
1. Azure Account
2. Azure Database for MySQL
3. Azure Cache for Redis
4. Azure App Service or AKS

### Step 1: Set Up Azure Database for MySQL
```bash
az mysql server create \
  --resource-group crm-rg \
  --name crm-mysql-server \
  --location eastus \
  --admin-user adminuser \
  --admin-password <your-password> \
  --sku-name B_Gen5_1
```

### Step 2: Set Up Azure Cache for Redis
```bash
az redis create \
  --resource-group crm-rg \
  --name crm-redis-cache \
  --location eastus \
  --sku Basic \
  --vm-size c0
```

### Step 3: Deploy to Azure App Service
```bash
# Create App Service plan
az appservice plan create \
  --name crm-plan \
  --resource-group crm-rg \
  --sku B1 \
  --is-linux

# Create web app
az webapp create \
  --resource-group crm-rg \
  --plan crm-plan \
  --name crm-webapp \
  --runtime "JAVA|8-jre8"

# Configure environment variables
az webapp config appsettings set \
  --resource-group crm-rg \
  --name crm-webapp \
  --settings \
    DB_URL="jdbc:mysql://<mysql-server>.mysql.database.azure.com:3306/crm?useSSL=true" \
    DB_USERNAME="adminuser@<mysql-server>" \
    DB_PASSWORD="<your-password>" \
    REDIS_HOST="<redis-cache>.redis.cache.windows.net" \
    REDIS_PORT="6379" \
    REDIS_PASSWORD="<redis-access-key>"

# Deploy JAR file
az webapp deployment source config-zip \
  --resource-group crm-rg \
  --name crm-webapp \
  --src target/crm-0.0.1-SNAPSHOT.jar
```

## GCP Deployment

### Prerequisites
1. GCP Account
2. Cloud SQL MySQL instance
3. Cloud Memorystore Redis instance
4. App Engine or GKE

### Step 1: Set Up Cloud SQL MySQL
```bash
gcloud sql instances create crm-mysql \
  --database-version=MYSQL_8_0 \
  --tier=db-f1-micro \
  --region=us-central1
```

### Step 2: Set Up Cloud Memorystore Redis
```bash
gcloud redis instances create crm-redis \
  --size=1 \
  --region=us-central1 \
  --redis-version=redis_6_x
```

### Step 3: Deploy to App Engine
Create `app.yaml`:
```yaml
runtime: java8
env: flex

env_variables:
  DB_URL: "jdbc:mysql:///<database>?cloudSqlInstance=<instance-connection-name>&socketFactory=com.google.cloud.sql.mysql.SocketFactory"
  DB_USERNAME: "root"
  DB_PASSWORD: "<your-password>"
  REDIS_HOST: "<redis-ip>"
  REDIS_PORT: "6379"
  SESSION_STORE_TYPE: "redis"
```

Deploy:
```bash
gcloud app deploy
```

## Local Development

### Running with Docker Compose
Create `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: crm
    ports:
      - "3306:3306"
  
  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
  
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:mysql://mysql:3306/crm?useSSL=false
      DB_USERNAME: root
      DB_PASSWORD: password
      REDIS_HOST: redis
      REDIS_PORT: 6379
    depends_on:
      - mysql
      - redis
```

Run:
```bash
docker-compose up
```

## Monitoring and Observability

### Health Checks
The application exposes actuator endpoints:
- `/appinfo/health` - Application health status
- `/appinfo/info` - Application information
- `/appinfo/metrics` - Application metrics

### Logging
- Logs are written to console in a structured format
- Compatible with CloudWatch (AWS), Azure Monitor, and Cloud Logging (GCP)
- Set `LOG_LEVEL` environment variable to control verbosity

### Session Monitoring
Monitor Redis for session metrics:
- Active sessions count
- Session creation/expiration rates
- Redis memory usage

## Scaling Considerations

### Horizontal Scaling
- Application is stateless and can scale horizontally
- No sticky sessions required
- Load balancer can distribute traffic evenly

### Database Connection Pooling
- HikariCP connection pool configured
- Adjust `DB_POOL_SIZE` based on instance count and load
- Formula: `pool_size = (core_count * 2) + effective_spindle_count`

### Redis Scaling
- For high traffic, use Redis cluster mode
- Configure Redis persistence for session durability
- Monitor Redis memory and adjust instance size

## Security Best Practices

1. **Secrets Management**
   - Use AWS Secrets Manager, Azure Key Vault, or GCP Secret Manager
   - Never commit credentials to source control
   - Rotate credentials regularly

2. **Network Security**
   - Use VPC/VNet for network isolation
   - Configure security groups to allow only necessary traffic
   - Enable SSL/TLS for database and Redis connections

3. **Application Security**
   - Keep dependencies updated
   - Enable HTTPS only in production
   - Configure CORS appropriately
   - Use strong session timeout values

## Troubleshooting

### Session Issues
- Verify Redis connectivity: `redis-cli -h <redis-host> ping`
- Check Redis logs for connection errors
- Verify `SESSION_STORE_TYPE=redis` is set

### Database Connection Issues
- Verify database connectivity from application instance
- Check security group rules
- Verify connection pool settings
- Check database logs for authentication errors

### Performance Issues
- Monitor database connection pool utilization
- Check Redis memory usage
- Review application logs for slow queries
- Use APM tools (AWS X-Ray, Azure Application Insights, GCP Cloud Trace)

## Support
For issues or questions, refer to:
- Spring Session documentation: https://spring.io/projects/spring-session
- Spring Boot documentation: https://spring.io/projects/spring-boot
- Cloud provider documentation
