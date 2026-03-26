# AWS Cloud Deployment Guide

## Overview
This application has been configured for cloud-native deployment on AWS with distributed session management using Redis.

## Architecture Components

### 1. Application Tier
- **Service**: AWS Elastic Beanstalk or ECS/EKS
- **Configuration**: Containerized Spring Boot application
- **Scaling**: Horizontal auto-scaling based on CPU/memory metrics

### 2. Session Store
- **Service**: AWS ElastiCache for Redis
- **Purpose**: Distributed session management for stateless horizontal scaling
- **Configuration**: 
  - Engine: Redis 6.x
  - Node Type: cache.t3.micro (or larger based on load)
  - Multi-AZ: Enabled for high availability

### 3. Database
- **Service**: AWS RDS for MySQL
- **Configuration**:
  - Engine: MySQL 5.7 or 8.0
  - Instance Type: db.t3.micro (or larger)
  - Multi-AZ: Enabled for production
  - Automated backups: Enabled

### 4. Load Balancer
- **Service**: Application Load Balancer (ALB)
- **Configuration**: 
  - Health check: /actuator/health
  - Sticky sessions: NOT required (Redis handles session state)

## Environment Variables

Configure the following environment variables in your AWS deployment:

```bash
# Database Configuration
DATABASE_URL=jdbc:mysql://<rds-endpoint>:3306/crm?useSSL=true
DATABASE_USERNAME=<db-username>
DATABASE_PASSWORD=<db-password>

# Redis Configuration
REDIS_HOST=<elasticache-endpoint>
REDIS_PORT=6379
REDIS_PASSWORD=<redis-password>
REDIS_SSL=true

# Application Configuration
SPRING_PROFILES_ACTIVE=cloud
PORT=8080
LOG_LEVEL=INFO

# Database Pool Configuration
DB_POOL_SIZE=20
DB_MIN_IDLE=5
DB_CONN_TIMEOUT=30000

# Session Configuration
SESSION_TIMEOUT=1800s
SESSION_TIMEOUT_MINUTES=30m

# Security Configuration
COOKIE_SECURE=true
```

## Deployment Steps

### Option 1: AWS Elastic Beanstalk

1. **Create Elastic Beanstalk Application**
   ```bash
   eb init -p docker crm-application --region us-east-1
   ```

2. **Configure Environment**
   ```bash
   eb create crm-prod-env \
     --instance-type t3.small \
     --envvars DATABASE_URL=<value>,REDIS_HOST=<value>,...
   ```

3. **Deploy Application**
   ```bash
   eb deploy
   ```

### Option 2: AWS ECS with Fargate

1. **Build and Push Docker Image**
   ```bash
   docker build -t crm-app:latest .
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
   docker tag crm-app:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
   docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
   ```

2. **Create ECS Task Definition**
   - Use the provided task-definition.json
   - Configure environment variables
   - Set up CloudWatch logging

3. **Create ECS Service**
   - Configure auto-scaling (2-10 tasks)
   - Attach to Application Load Balancer
   - Enable service discovery

### Option 3: AWS EKS (Kubernetes)

1. **Create EKS Cluster**
   ```bash
   eksctl create cluster --name crm-cluster --region us-east-1 --nodes 2
   ```

2. **Deploy Application**
   ```bash
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   ```

## Infrastructure as Code

### CloudFormation Template
See `cloudformation-template.yaml` for complete infrastructure setup including:
- VPC and networking
- RDS MySQL instance
- ElastiCache Redis cluster
- ECS cluster and services
- Application Load Balancer
- Security groups and IAM roles

### Terraform Configuration
See `terraform/` directory for modular infrastructure setup.

## Monitoring and Observability

### CloudWatch Metrics
- Application metrics: /actuator/metrics
- Custom metrics: Session count, request latency
- Alarms: CPU > 80%, Memory > 80%, Error rate > 5%

### CloudWatch Logs
- Application logs: /aws/ecs/crm-app
- Access logs: ALB access logs
- Log retention: 30 days

### X-Ray Tracing
- Enable X-Ray daemon in ECS task
- Add X-Ray SDK to application (optional enhancement)

## Security Considerations

1. **Network Security**
   - Application in private subnets
   - RDS and ElastiCache in isolated subnets
   - Security groups restrict access

2. **Secrets Management**
   - Use AWS Secrets Manager for database credentials
   - Use AWS Systems Manager Parameter Store for configuration
   - Rotate credentials regularly

3. **Encryption**
   - RDS encryption at rest enabled
   - ElastiCache encryption in transit enabled
   - ALB uses HTTPS with ACM certificate

## Cost Optimization

1. **Right-sizing**
   - Start with t3.small instances
   - Monitor and adjust based on metrics

2. **Auto-scaling**
   - Scale down during off-peak hours
   - Use spot instances for non-production

3. **Reserved Instances**
   - Purchase RDS reserved instances for production
   - Consider Savings Plans for compute

## Disaster Recovery

1. **Backup Strategy**
   - RDS automated backups: Daily
   - RDS snapshots: Weekly
   - Retention: 7 days

2. **High Availability**
   - Multi-AZ RDS deployment
   - Multi-AZ ElastiCache replication
   - Multi-AZ ECS/EKS deployment

3. **Recovery Procedures**
   - RTO: 1 hour
   - RPO: 5 minutes
   - Documented runbooks in wiki

## Testing

1. **Local Testing with Docker Compose**
   ```bash
   docker-compose up
   ```

2. **Load Testing**
   - Use Apache JMeter or Gatling
   - Test session persistence across instances
   - Verify auto-scaling behavior

3. **Chaos Engineering**
   - Terminate random instances
   - Simulate Redis failure
   - Test database failover

## Support and Troubleshooting

### Common Issues

1. **Session Loss**
   - Verify Redis connectivity
   - Check Redis memory usage
   - Review session timeout configuration

2. **Database Connection Issues**
   - Verify security group rules
   - Check connection pool settings
   - Review RDS performance metrics

3. **High Latency**
   - Check Redis latency
   - Review database query performance
   - Analyze ALB metrics

### Health Checks

- Application: http://localhost:8080/actuator/health
- Redis: `redis-cli ping`
- Database: Check RDS console

## Next Steps

1. Set up CI/CD pipeline (AWS CodePipeline)
2. Implement blue-green deployment
3. Add application performance monitoring (APM)
4. Configure WAF for security
5. Implement rate limiting
