# CRM LiveLog - Cloud-Native Deployment Guide

## Overview
This guide provides instructions for deploying the CRM LiveLog application to AWS with distributed session management using Redis. The application has been updated to be fully cloud-ready and stateless.

## Cloud Readiness Features

### ✅ Distributed Session Management
- **Redis-backed sessions**: HTTP sessions stored in ElastiCache Redis
- **No server affinity**: Any instance can handle any request
- **Horizontal scaling**: Add/remove instances without data loss
- **Session persistence**: Sessions survive instance restarts

### ✅ Stateless Architecture
- **No local state**: All state externalized to Redis and database
- **Request-scoped data**: View classes use only model data
- **Load balancer compatible**: Works with standard round-robin load balancing
- **Cloud-native patterns**: Follows 12-factor app principles

### ✅ Environment-Based Configuration
- **No hardcoded credentials**: All sensitive data via environment variables
- **AWS Secrets Manager**: Integration for credential management
- **Flexible configuration**: Easy to configure per environment

## Prerequisites

### Local Development
- Docker and Docker Compose
- Java 8 or higher
- Maven 3.6+

### AWS Deployment
- AWS CLI configured
- AWS account with appropriate permissions
- ECR repository created
- VPC with public and private subnets
- RDS MySQL database

## Local Development with Docker Compose

### 1. Start the Application
```bash
docker-compose up -d
```

This starts:
- Redis (port 6379) - Distributed session store
- MySQL (port 3306) - Database
- App Instance 1 (port 8081) - Application instance 1
- App Instance 2 (port 8082) - Application instance 2
- Nginx (port 80) - Load balancer

### 2. Test Distributed Sessions
```bash
# Create a session on instance 1
curl -c cookies.txt http://localhost:8081/login

# Use the session on instance 2
curl -b cookies.txt http://localhost:8082/user/menu
```

The session should work across both instances, demonstrating distributed session management.

### 3. Test Horizontal Scaling
```bash
# Scale to 4 instances
docker-compose up -d --scale app1=2 --scale app2=2

# Verify all instances share session data
for i in {1..10}; do
  curl -b cookies.txt http://localhost/user/menu
done
```

### 4. Stop the Application
```bash
docker-compose down
```

## AWS Deployment

### Option 1: Using CloudFormation (Recommended)

#### 1. Create Secrets in AWS Secrets Manager
```bash
# Database credentials
aws secretsmanager create-secret \
  --name crm/database/url \
  --secret-string "jdbc:mysql://your-rds-endpoint:3306/crm?useSSL=true"

aws secretsmanager create-secret \
  --name crm/database/username \
  --secret-string "your-db-username"

aws secretsmanager create-secret \
  --name crm/database/password \
  --secret-string "your-db-password"

# Redis password (if using AUTH)
aws secretsmanager create-secret \
  --name crm/redis/password \
  --secret-string "your-redis-password"
```

#### 2. Build and Push Docker Image
```bash
# Set environment variables
export AWS_ACCOUNT_ID=123456789012
export AWS_REGION=us-east-1

# Build image
docker build -t crm-livelog:latest .

# Create ECR repository
aws ecr create-repository --repository-name crm-livelog --region $AWS_REGION

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin \
  $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Tag and push
docker tag crm-livelog:latest \
  $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/crm-livelog:latest

docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/crm-livelog:latest
```

#### 3. Deploy CloudFormation Stack
```bash
aws cloudformation create-stack \
  --stack-name crm-livelog-stack \
  --template-body file://aws/cloudformation-template.yml \
  --parameters \
    ParameterKey=VpcId,ParameterValue=vpc-xxxxx \
    ParameterKey=PrivateSubnetIds,ParameterValue=subnet-xxxxx\\,subnet-yyyyy \
    ParameterKey=PublicSubnetIds,ParameterValue=subnet-aaaaa\\,subnet-bbbbb \
    ParameterKey=DatabaseEndpoint,ParameterValue=your-rds-endpoint \
    ParameterKey=ContainerImage,ParameterValue=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/crm-livelog:latest \
    ParameterKey=DesiredCount,ParameterValue=2 \
    ParameterKey=MinCapacity,ParameterValue=2 \
    ParameterKey=MaxCapacity,ParameterValue=10 \
  --capabilities CAPABILITY_IAM \
  --region $AWS_REGION
```

#### 4. Monitor Deployment
```bash
# Watch stack creation
aws cloudformation wait stack-create-complete \
  --stack-name crm-livelog-stack \
  --region $AWS_REGION

# Get load balancer URL
aws cloudformation describe-stacks \
  --stack-name crm-livelog-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerURL`].OutputValue' \
  --output text \
  --region $AWS_REGION
```

### Option 2: Using Deployment Script

```bash
# Set environment variables
export AWS_ACCOUNT_ID=123456789012
export AWS_REGION=us-east-1

# Run deployment script
./aws/deploy.sh
```

## Architecture

### AWS Infrastructure
```
┌─────────────────────────────────────────────────────────────┐
│                  Application Load Balancer                  │
│              (No Sticky Sessions Required)                  │
└────────────────┬────────────────┬───────────────────────────┘
                 │                │
        ┌────────▼────────┐  ┌───▼──────────────┐
        │  ECS Task 1     │  │  ECS Task 2      │
        │  (Fargate)      │  │  (Fargate)       │
        │  Stateless      │  │  Stateless       │
        └────────┬────────┘  └───┬──────────────┘
                 │                │
                 └────────┬───────┘
                          │
                 ┌────────▼────────┐
                 │  ElastiCache    │
                 │  Redis Cluster  │
                 │  (Multi-AZ)     │
                 │  Session Store  │
                 └────────┬────────┘
                          │
                 ┌────────▼────────┐
                 │  RDS MySQL      │
                 │  (Multi-AZ)     │
                 │  Database       │
                 └─────────────────┘
```

### Components
1. **Application Load Balancer**: Distributes traffic across ECS tasks
2. **ECS Fargate Tasks**: Stateless application instances
3. **ElastiCache Redis**: Distributed session storage (Multi-AZ)
4. **RDS MySQL**: Application database (Multi-AZ)
5. **CloudWatch**: Logging and monitoring
6. **Secrets Manager**: Credential management

## Configuration

### Environment Variables

#### Required
- `DATABASE_URL`: JDBC connection string
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `REDIS_HOST`: Redis server hostname
- `REDIS_PORT`: Redis server port (default: 6379)

#### Optional
- `REDIS_PASSWORD`: Redis authentication password
- `SESSION_TIMEOUT`: Session timeout in seconds (default: 1800)
- `REDIS_POOL_MAX_ACTIVE`: Max active connections (default: 8)
- `REDIS_POOL_MAX_IDLE`: Max idle connections (default: 8)
- `REDIS_POOL_MIN_IDLE`: Min idle connections (default: 0)

### Application Profiles
- `cloud`: Cloud-specific configuration
- `aws`: AWS-specific configuration
- `local`: Local development configuration

## Monitoring

### CloudWatch Metrics
- `ECSServiceAverageCPUUtilization`: CPU usage across tasks
- `ECSServiceAverageMemoryUtilization`: Memory usage across tasks
- `TargetResponseTime`: Application response time
- `HealthyHostCount`: Number of healthy tasks
- `UnHealthyHostCount`: Number of unhealthy tasks

### Application Metrics
- Session creation/destruction rate
- Redis connection pool utilization
- Database connection pool utilization
- Request count per instance

### Health Checks
- **Application**: `/actuator/health`
- **ECS Task**: Container health check every 30s
- **Target Group**: ALB health check every 30s

## Auto Scaling

### Scaling Policies
1. **CPU-based**: Scale when CPU > 70%
2. **Memory-based**: Scale when memory > 80%

### Scaling Configuration
- **Min Capacity**: 2 tasks
- **Max Capacity**: 10 tasks
- **Scale Out Cooldown**: 60 seconds
- **Scale In Cooldown**: 300 seconds

## Security

### Network Security
- Application runs in private subnets
- ALB in public subnets
- Security groups restrict traffic
- Redis and RDS in private subnets

### Data Security
- Redis encryption at rest and in transit
- RDS encryption at rest
- Secrets stored in AWS Secrets Manager
- IAM roles for service authentication

### Best Practices
- Use AWS Secrets Manager for credentials
- Enable VPC Flow Logs
- Enable CloudTrail for audit logging
- Use AWS WAF for application protection
- Implement least privilege IAM policies

## Troubleshooting

### Session Issues
```bash
# Check Redis connectivity
redis-cli -h <redis-endpoint> -p 6379 ping

# Check session keys in Redis
redis-cli -h <redis-endpoint> -p 6379 keys "spring:session:*"

# Monitor Redis operations
redis-cli -h <redis-endpoint> -p 6379 monitor
```

### Application Issues
```bash
# View ECS task logs
aws logs tail /ecs/crm-livelog --follow

# Check task health
aws ecs describe-tasks \
  --cluster crm-cluster \
  --tasks <task-id>

# Check service events
aws ecs describe-services \
  --cluster crm-cluster \
  --services crm-livelog-service
```

### Load Balancer Issues
```bash
# Check target health
aws elbv2 describe-target-health \
  --target-group-arn <target-group-arn>

# View ALB access logs
aws s3 cp s3://<alb-logs-bucket>/ . --recursive
```

## Cost Optimization

### Recommendations
1. Use Fargate Spot for non-production environments
2. Right-size ECS task CPU/memory
3. Use Redis cache.t3.micro for development
4. Enable RDS auto-scaling storage
5. Use CloudWatch Logs retention policies

### Estimated Monthly Costs (us-east-1)
- **ECS Fargate** (2 tasks, 0.5 vCPU, 1GB): ~$30
- **ElastiCache Redis** (cache.t3.micro, Multi-AZ): ~$25
- **RDS MySQL** (db.t3.micro, Multi-AZ): ~$30
- **Application Load Balancer**: ~$20
- **Data Transfer**: Variable
- **Total**: ~$105/month (development)

## Rollback

### CloudFormation Rollback
```bash
# Automatic rollback on failure
aws cloudformation delete-stack \
  --stack-name crm-livelog-stack \
  --region $AWS_REGION
```

### Manual Rollback
```bash
# Update service to previous task definition
aws ecs update-service \
  --cluster crm-cluster \
  --service crm-livelog-service \
  --task-definition crm-livelog-task:PREVIOUS_REVISION
```

## Support

For issues or questions:
1. Check CloudWatch Logs: `/ecs/crm-livelog`
2. Review ECS service events
3. Check Redis connectivity
4. Verify Secrets Manager values
5. Review security group rules

## Additional Resources

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [ElastiCache Redis Documentation](https://docs.aws.amazon.com/elasticache/)
- [Spring Session Documentation](https://docs.spring.io/spring-session/docs/current/reference/html5/)
- [12-Factor App Methodology](https://12factor.net/)
