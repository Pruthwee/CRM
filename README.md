# CRM Application - Cloud-Ready Version

## Overview
This CRM (Customer Relationship Management) application has been modernized for cloud deployment with distributed session management, stateless architecture, and cloud-native patterns.

## Cloud-Ready Features

### ✅ Distributed Session Management
- **Spring Session with Redis** for stateless horizontal scaling
- No sticky sessions required
- Session persistence across instance restarts
- Compatible with AWS ElastiCache, Azure Cache for Redis, GCP Memorystore

### ✅ Stateless Architecture
- All view components (CSV, Excel, PDF) are stateless
- No local file system dependencies
- Request-scoped data processing
- Thread-safe concurrent request handling

### ✅ Environment-Based Configuration
- All configuration via environment variables
- No hardcoded credentials or endpoints
- Separate profiles for development and production
- 12-factor app compliant

### ✅ Connection Pooling
- HikariCP for database connection pooling
- Configurable pool sizes and timeouts
- Optimized for cloud environments

### ✅ Container-Ready
- Multi-stage Dockerfile for optimized images
- Docker Compose for local development
- Kubernetes manifests for orchestration
- Health checks and readiness probes

## Quick Start

### Local Development with Docker Compose

1. **Prerequisites**
   - Docker and Docker Compose installed
   - 8080, 3306, and 6379 ports available

2. **Start the application**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Application: http://localhost:8080
   - Health Check: http://localhost:8080/appinfo/health

4. **Stop the application**
   ```bash
   docker-compose down
   ```

### Local Development without Docker

1. **Prerequisites**
   - Java 8 or higher
   - Maven 3.6+
   - MySQL 8.0
   - Redis 6.0

2. **Set up database**
   ```sql
   CREATE DATABASE crm;
   CREATE USER 'crmuser'@'localhost' IDENTIFIED BY 'crmpass';
   GRANT ALL PRIVILEGES ON crm.* TO 'crmuser'@'localhost';
   ```

3. **Start Redis**
   ```bash
   redis-server
   ```

4. **Configure environment variables**
   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/crm?useSSL=false
   export DB_USERNAME=crmuser
   export DB_PASSWORD=crmpass
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   ```

5. **Build and run**
   ```bash
   mvn clean package
   java -jar target/crm-0.0.1-SNAPSHOT.jar
   ```

## Cloud Deployment

### AWS Deployment

#### Option 1: Elastic Beanstalk

1. **Install EB CLI**
   ```bash
   pip install awsebcli
   ```

2. **Initialize and deploy**
   ```bash
   eb init -p java-8 crm-application
   eb create crm-prod-env
   eb deploy
   ```

3. **Configure environment variables in EB Console**
   - Set RDS endpoint, credentials
   - Set ElastiCache endpoint
   - Configure other environment variables

#### Option 2: ECS/Fargate

1. **Build and push Docker image**
   ```bash
   docker build -t crm-app:latest .
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
   docker tag crm-app:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
   docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
   ```

2. **Create ECS task definition and service**
   - Use AWS Console or CloudFormation
   - Configure environment variables
   - Set up load balancer

#### Option 3: EKS (Kubernetes)

1. **Create EKS cluster**
   ```bash
   eksctl create cluster --name crm-cluster --region us-east-1
   ```

2. **Deploy application**
   ```bash
   kubectl apply -f k8s-deployment.yaml
   ```

3. **Get load balancer URL**
   ```bash
   kubectl get service crm-service -n crm-app
   ```

### Azure Deployment

#### Option 1: Azure App Service

1. **Create resources**
   ```bash
   az group create --name crm-rg --location eastus
   az mysql server create --resource-group crm-rg --name crm-mysql --admin-user adminuser --admin-password <password>
   az redis create --resource-group crm-rg --name crm-redis --location eastus --sku Basic --vm-size c0
   ```

2. **Deploy application**
   ```bash
   az webapp create --resource-group crm-rg --plan crm-plan --name crm-webapp --runtime "JAVA|8-jre8"
   az webapp deployment source config-zip --resource-group crm-rg --name crm-webapp --src target/crm-0.0.1-SNAPSHOT.jar
   ```

#### Option 2: AKS (Kubernetes)

1. **Create AKS cluster**
   ```bash
   az aks create --resource-group crm-rg --name crm-aks --node-count 3 --enable-addons monitoring
   az aks get-credentials --resource-group crm-rg --name crm-aks
   ```

2. **Deploy application**
   ```bash
   kubectl apply -f k8s-deployment.yaml
   ```

### GCP Deployment

#### Option 1: App Engine

1. **Create app.yaml**
   ```yaml
   runtime: java8
   env: flex
   env_variables:
     DB_URL: "jdbc:mysql:///<database>?cloudSqlInstance=<instance>&socketFactory=com.google.cloud.sql.mysql.SocketFactory"
     REDIS_HOST: "<redis-ip>"
   ```

2. **Deploy**
   ```bash
   gcloud app deploy
   ```

#### Option 2: GKE (Kubernetes)

1. **Create GKE cluster**
   ```bash
   gcloud container clusters create crm-cluster --num-nodes=3 --zone=us-central1-a
   gcloud container clusters get-credentials crm-cluster --zone=us-central1-a
   ```

2. **Deploy application**
   ```bash
   kubectl apply -f k8s-deployment.yaml
   ```

## Configuration

### Environment Variables

#### Required Variables
- `DB_URL`: Database JDBC URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `REDIS_HOST`: Redis server hostname

#### Optional Variables (with defaults)
- `DB_DDL_AUTO`: Hibernate DDL mode (default: update)
- `DB_POOL_SIZE`: Connection pool size (default: 10)
- `REDIS_PORT`: Redis port (default: 6379)
- `REDIS_PASSWORD`: Redis password (default: empty)
- `SESSION_TIMEOUT`: Session timeout in seconds (default: 1800)
- `LOG_LEVEL`: Logging level (default: INFO)
- `THYMELEAF_CACHE`: Enable template caching (default: false)

### Configuration Profiles

#### Default Profile (application.properties)
- For local development
- Relaxed security settings
- Debug logging enabled

#### Cloud Profile (application-cloud.properties)
- For production deployment
- Optimized connection pools
- Enhanced security
- Production logging

Activate cloud profile:
```bash
export SPRING_PROFILES_ACTIVE=cloud
```

## Monitoring and Observability

### Health Checks
- **Endpoint**: `/appinfo/health`
- **Response**: JSON with application health status
- **Use**: Load balancer health checks, Kubernetes probes

### Metrics
- **Endpoint**: `/appinfo/metrics`
- **Response**: Application metrics
- **Use**: Monitoring and alerting

### Logging
- Console logging in structured format
- Compatible with CloudWatch, Azure Monitor, Cloud Logging
- Configurable log levels via `LOG_LEVEL` environment variable

## Scaling

### Horizontal Scaling
The application is designed for horizontal scaling:
- Stateless architecture
- Distributed session management
- No local file system dependencies
- Thread-safe request processing

### Auto-Scaling Configuration

#### AWS Auto Scaling
- Configured in `.ebextensions/01-environment.config`
- CPU-based scaling (30-70% utilization)
- Min: 2 instances, Max: 10 instances

#### Kubernetes HPA
- Configured in `k8s-deployment.yaml`
- CPU and memory-based scaling
- Min: 3 pods, Max: 10 pods

## Security

### Best Practices Implemented
- ✅ No hardcoded credentials
- ✅ Environment-based configuration
- ✅ Secure session cookies (HTTP-only, Secure flag)
- ✅ Connection pooling with timeouts
- ✅ Non-root container user
- ✅ Health check endpoints

### Additional Recommendations
- Use secrets management (AWS Secrets Manager, Azure Key Vault, GCP Secret Manager)
- Enable SSL/TLS for database and Redis connections
- Configure network security groups/firewalls
- Enable HTTPS only in production
- Implement rate limiting
- Regular security updates

## Troubleshooting

### Common Issues

#### Session not persisting
- Verify Redis connectivity: `redis-cli -h <redis-host> ping`
- Check `SESSION_STORE_TYPE=redis` is set
- Review Redis logs for errors

#### Database connection failures
- Verify database endpoint and credentials
- Check security group rules
- Verify connection pool settings
- Review database logs

#### Application not starting
- Check environment variables are set correctly
- Review application logs
- Verify dependencies (MySQL, Redis) are accessible
- Check health check endpoint

### Debug Mode
Enable debug logging:
```bash
export LOG_LEVEL=DEBUG
```

## Architecture

### Components
- **Web Layer**: Spring MVC controllers
- **Service Layer**: Business logic
- **Repository Layer**: JPA repositories
- **View Layer**: Thymeleaf templates, CSV/Excel/PDF views
- **Session Store**: Redis (distributed)
- **Database**: MySQL (with HikariCP pooling)

### Cloud-Native Patterns
- **Stateless Services**: No local state storage
- **Externalized Configuration**: Environment variables
- **Distributed Session**: Redis-backed sessions
- **Health Checks**: Actuator endpoints
- **Connection Pooling**: HikariCP
- **Container-Ready**: Docker support

## Performance Optimization

### Database
- Connection pooling configured
- Adjust pool size based on load
- Use read replicas for read-heavy workloads

### Redis
- Connection pooling configured
- Use Redis cluster for high availability
- Configure persistence for session durability

### Application
- Template caching enabled in production
- Response compression enabled
- JVM tuning for containerized environments

## Support and Documentation

### Additional Resources
- [Cloud Deployment Guide](CLOUD_DEPLOYMENT_GUIDE.md)
- [Spring Session Documentation](https://spring.io/projects/spring-session)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

### Getting Help
For issues or questions:
1. Check the troubleshooting section
2. Review application logs
3. Check cloud provider documentation
4. Review Spring Boot/Session documentation

## License
[Your License Here]

## Contributors
[Your Team/Contributors Here]
