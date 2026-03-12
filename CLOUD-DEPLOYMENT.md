# Cloud Deployment Guide for CRM Application

## Overview
This CRM application has been modernized for cloud deployment with the following cloud-ready features:

### Cloud-Ready Features Implemented

#### 1. **Configuration Management**
- ✅ Environment variable-based configuration
- ✅ Externalized configuration for different environments
- ✅ No hardcoded credentials or connection strings
- ✅ Support for cloud configuration services

#### 2. **Stateless Architecture**
- ✅ HTTP sessions stored in database (JDBC) for horizontal scaling
- ✅ No local file system dependencies
- ✅ In-memory PDF/CSV/Excel generation
- ✅ Stateless REST API endpoints

#### 3. **Database & Persistence**
- ✅ HikariCP connection pooling for cloud databases
- ✅ JPA/Hibernate for database abstraction
- ✅ Support for AWS RDS, Azure SQL, Google Cloud SQL
- ✅ Proper connection timeout and pool management

#### 4. **RESTful API Design**
- ✅ REST API endpoints for all major operations
- ✅ Compatible with API gateways and load balancers
- ✅ Proper HTTP status codes and error handling
- ✅ JSON response format support

#### 5. **Monitoring & Observability**
- ✅ Spring Boot Actuator for health checks
- ✅ Prometheus metrics export
- ✅ Structured logging for cloud log aggregation
- ✅ Health check endpoints for load balancers

#### 6. **Containerization**
- ✅ Multi-stage Dockerfile for optimized images
- ✅ Docker Compose for local testing
- ✅ Non-root user for security
- ✅ Health checks in container

#### 7. **Cloud Platform Support**
- ✅ AWS Elastic Beanstalk configuration
- ✅ Kubernetes deployment manifests
- ✅ Horizontal Pod Autoscaling (HPA)
- ✅ Load balancer integration

## Deployment Options

### Option 1: AWS Elastic Beanstalk

1. **Prerequisites**
   - AWS CLI installed and configured
   - EB CLI installed

2. **Deploy**
   ```bash
   # Initialize Elastic Beanstalk
   eb init -p java-11 crm-application --region us-east-1
   
   # Create environment
   eb create crm-prod --database.engine mysql --database.username admin
   
   # Deploy application
   mvn clean package
   eb deploy
   ```

3. **Configure Environment Variables**
   ```bash
   eb setenv DATABASE_URL=jdbc:mysql://your-rds-endpoint:3306/crm \
             DATABASE_USERNAME=admin \
             DATABASE_PASSWORD=your-password \
             SPRING_PROFILES_ACTIVE=cloud
   ```

### Option 2: AWS ECS/Fargate

1. **Build and Push Docker Image**
   ```bash
   # Build image
   docker build -t crm-app:latest .
   
   # Tag for ECR
   docker tag crm-app:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
   
   # Push to ECR
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
   docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
   ```

2. **Create ECS Task Definition and Service**
   - Use AWS Console or CloudFormation
   - Configure environment variables
   - Set up Application Load Balancer
   - Configure Auto Scaling

### Option 3: Kubernetes (EKS, AKS, GKE)

1. **Create Secrets**
   ```bash
   kubectl create secret generic crm-secrets \
     --from-literal=database-url='jdbc:mysql://your-db:3306/crm' \
     --from-literal=database-username='admin' \
     --from-literal=database-password='your-password'
   ```

2. **Deploy Application**
   ```bash
   kubectl apply -f k8s/deployment.yaml
   ```

3. **Verify Deployment**
   ```bash
   kubectl get pods
   kubectl get svc
   kubectl logs -f deployment/crm-application
   ```

### Option 4: Docker Compose (Local/Development)

1. **Start Services**
   ```bash
   docker-compose up -d
   ```

2. **View Logs**
   ```bash
   docker-compose logs -f crm-app
   ```

3. **Stop Services**
   ```bash
   docker-compose down
   ```

## Environment Variables

### Required Variables
- `DATABASE_URL` - JDBC connection string
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password

### Optional Variables
- `SPRING_PROFILES_ACTIVE` - Active Spring profile (default: cloud)
- `SERVER_PORT` - Application port (default: 8080)
- `LOG_LEVEL` - Logging level (default: INFO)
- `DB_POOL_SIZE` - Connection pool size (default: 20)
- `DB_POOL_MIN_IDLE` - Minimum idle connections (default: 5)

## Health Checks

- **Health Endpoint**: `GET /appinfo/health`
- **Metrics Endpoint**: `GET /appinfo/metrics`
- **Prometheus Metrics**: `GET /appinfo/prometheus`

## REST API Endpoints

### Customer API
- `GET /customer/api/list` - Get all customers
- `GET /customer/api/{id}` - Get customer by ID

### Contract API
- `GET /contract/api/list` - Get all contracts
- `GET /contract/api/{id}` - Get contract by ID

### User API
- `GET /user/api/list` - Get all users
- `GET /user/api/{id}` - Get user by ID

### CSV Export API
- `GET /api/csv/customers` - Export customers as CSV
- `GET /api/csv/customers/{id}` - Export customer as CSV
- `GET /api/csv/customers/json` - Get customers as JSON

## Scaling Considerations

### Horizontal Scaling
- Application is stateless and can scale horizontally
- Sessions stored in database (shared across instances)
- No local file system dependencies
- Load balancer distributes traffic across instances

### Database Scaling
- Use managed database services (RDS, Azure SQL, Cloud SQL)
- Configure connection pooling appropriately
- Consider read replicas for read-heavy workloads

### Caching
- Application supports distributed caching
- Can integrate with Redis or Memcached
- Cache configuration in application properties

## Security Best Practices

1. **Secrets Management**
   - Use AWS Secrets Manager, Azure Key Vault, or GCP Secret Manager
   - Never commit secrets to version control
   - Rotate credentials regularly

2. **Network Security**
   - Use security groups/network policies
   - Enable HTTPS/TLS
   - Restrict database access to application subnet

3. **Container Security**
   - Application runs as non-root user
   - Minimal base image (JRE only)
   - Regular security updates

## Monitoring & Logging

### Logging
- Structured logging to stdout/stderr
- Compatible with CloudWatch, Stackdriver, Azure Monitor
- Log aggregation with ELK, Splunk, or cloud-native solutions

### Metrics
- Prometheus metrics exposed
- Integration with Grafana for visualization
- CloudWatch/Azure Monitor/Stackdriver integration

### Alerting
- Configure alerts for:
  - High error rates
  - High response times
  - Database connection pool exhaustion
  - Memory/CPU usage

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
   - Check DATABASE_URL format
   - Verify security group rules
   - Check database credentials

2. **Session Issues**
   - Ensure SPRING_SESSION tables are created
   - Check database connectivity
   - Verify session timeout configuration

3. **Health Check Failures**
   - Check application logs
   - Verify database connectivity
   - Ensure sufficient resources (CPU/Memory)

## Support

For issues or questions, please refer to:
- Application logs: `kubectl logs` or CloudWatch
- Health endpoint: `/appinfo/health`
- Metrics endpoint: `/appinfo/metrics`
