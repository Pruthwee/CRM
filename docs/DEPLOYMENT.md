# CRM Application - Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Development Setup](#local-development-setup)
4. [Docker Deployment](#docker-deployment)
5. [AWS ECS Fargate Deployment](#aws-ecs-fargate-deployment)
6. [Configuration Management](#configuration-management)
7. [Monitoring and Logging](#monitoring-and-logging)
8. [Troubleshooting](#troubleshooting)
9. [Security Considerations](#security-considerations)

---

## Overview

This guide provides comprehensive instructions for deploying the CRM application using Docker and AWS ECS Fargate. The application is a Spring Boot 1.5.10 application running on Java 8.

**Application Details:**
- **Framework:** Spring Boot 1.5.10.RELEASE
- **Java Version:** 1.8
- **Build Tool:** Maven
- **Package Type:** JAR
- **Application Port:** 8080
- **Health Endpoint:** /actuator/health
- **Database:** MySQL (external service)

---

## Prerequisites

### Required Software
- **Docker:** Version 20.10 or higher
- **Docker Compose:** Version 1.29 or higher
- **AWS CLI:** Version 2.x (for ECS deployment)
- **Git:** For cloning the repository
- **Java 8 JDK:** For local development (optional)
- **Maven 3.6+:** For local builds (optional)

### AWS Requirements (for ECS Fargate)
- AWS Account with appropriate permissions
- AWS CLI configured with credentials
- VPC with at least 2 subnets in different availability zones
- Security groups configured for application access
- IAM roles:
  - `ecsTaskExecutionRole` - For ECS to pull images and write logs
  - `ecsTaskRole` - For application to access AWS services (optional)

### External Services
- **MySQL Database:** Version 5.7 or higher
  - Host, port, database name, username, and password required
  - Database should be accessible from the container network

---

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd CRM
```

### 2. Configure Environment Variables
Create a `.env` file in the project root:

```env
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=crm
DB_USERNAME=root
DB_PASSWORD=password

# Java/JVM Configuration
JAVA_OPTS=-Xmx512m -Xms256m

# Spring Configuration
SPRING_PROFILES_ACTIVE=dev
```

### 3. Build and Run with Docker Compose
```bash
# Build and start the application
docker-compose up --build

# Run in detached mode
docker-compose up -d

# View logs
docker-compose logs -f crm-app

# Stop the application
docker-compose down
```

### 4. Access the Application
- **Application URL:** http://localhost:8080
- **Health Check:** http://localhost:8080/actuator/health

---

## Docker Deployment

### Building the Docker Image

#### Using build-push.sh (Linux/macOS)
```bash
cd scripts
chmod +x build-push.sh
./build-push.sh
```

#### Using build-push.bat (Windows)
```cmd
cd scripts
build-push.bat
```

The script will:
1. Prompt for registry selection (AWS ECR or Docker Hub)
2. Request registry credentials and configuration
3. Build the Docker image using multi-stage build
4. Tag the image appropriately
5. Push the image to the selected registry

### Manual Docker Build
```bash
# Build the image
docker build -t crm:latest .

# Tag for registry
docker tag crm:latest <registry>/crm:latest

# Push to registry
docker push <registry>/crm:latest
```

### Docker Image Details
- **Base Image:** eclipse-temurin:8-jdk
- **Build Tool:** Maven 3.9.4 with Eclipse Temurin 8
- **Multi-stage Build:** Yes (builder + runtime)
- **Image Size:** ~200-250 MB (runtime stage)
- **Non-root User:** appuser (for security)

---

## AWS ECS Fargate Deployment

### Prerequisites Setup

#### 1. Create IAM Roles

**ECS Task Execution Role:**
```bash
aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'

aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
```

**ECS Task Role (optional):**
```bash
aws iam create-role \
  --role-name ecsTaskRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'
```

#### 2. Create CloudWatch Log Group
```bash
aws logs create-log-group --log-group-name /ecs/crm --region us-east-1
```

#### 3. Configure Security Group
Create a security group that allows:
- **Inbound:** Port 8080 (application) from ALB or internet
- **Outbound:** All traffic (for database and internet access)

```bash
# Create security group
aws ec2 create-security-group \
  --group-name crm-ecs-sg \
  --description "Security group for CRM ECS tasks" \
  --vpc-id <your-vpc-id>

# Add inbound rule for application port
aws ec2 authorize-security-group-ingress \
  --group-id <security-group-id> \
  --protocol tcp \
  --port 8080 \
  --cidr 0.0.0.0/0
```

### Deployment Process

#### Using deploy-image.sh (Linux/macOS)
```bash
cd scripts
chmod +x deploy-image.sh
./deploy-image.sh
```

#### Using deploy-image.bat (Windows)
```cmd
cd scripts
deploy-image.bat
```

The deployment script will:
1. Prompt for AWS region and ECS cluster name
2. Request network configuration (VPC, subnets, security groups)
3. Request database connection details
4. Request container image URI
5. Ask if load balancer is needed
6. Create/update ECS cluster
7. Create CloudWatch log group
8. Create Application Load Balancer and Target Group (if requested)
9. Register ECS task definition
10. Create or update ECS service
11. Wait for service to become stable
12. Display deployment status and access information

### ECS Task Definition Details

**Resource Allocation:**
- **CPU:** 512 (.5 vCPU)
- **Memory:** 1024 MB (1 GB)
- **Launch Type:** FARGATE
- **Network Mode:** awsvpc

**Container Configuration:**
- **Port:** 8080
- **Health Check:** /actuator/health
- **Logging:** CloudWatch Logs (/ecs/crm)

**Valid Fargate CPU/Memory Combinations:**
- CPU: 256 → Memory: 512, 1024, 2048 MB
- CPU: 512 → Memory: 1024, 2048, 3072, 4096 MB
- CPU: 1024 → Memory: 2048-8192 MB (1 GB increments)
- CPU: 2048 → Memory: 4096-16384 MB (1 GB increments)
- CPU: 4096 → Memory: 8192-30720 MB (1 GB increments)

### ECS Service Configuration

**Deployment Settings:**
- **Desired Count:** 2 tasks
- **Maximum Percent:** 200%
- **Minimum Healthy Percent:** 50%
- **Deployment Circuit Breaker:** Enabled with rollback

**Network Configuration:**
- **Subnets:** At least 2 in different AZs
- **Security Groups:** Application security group
- **Public IP:** Enabled (for internet access)

**Load Balancer (if enabled):**
- **Type:** Application Load Balancer
- **Target Type:** IP (required for Fargate)
- **Health Check Path:** /actuator/health
- **Health Check Interval:** 30 seconds
- **Grace Period:** 300 seconds

---

## Configuration Management

### Environment Variables

The application uses the following environment variables:

#### Java/JVM Configuration
```env
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

#### Spring Boot Configuration
```env
SPRING_PROFILES_ACTIVE=docker
```

#### Database Configuration
```env
DB_HOST=<database-host>
DB_PORT=3306
DB_NAME=crm
DB_USERNAME=<database-username>
DB_PASSWORD=<database-password>
```

#### System Configuration
```env
TZ=UTC
```

### Spring Profiles

The application supports multiple Spring profiles:
- **default:** Local development with H2 database
- **docker:** Docker deployment with MySQL
- **production:** Production deployment with optimized settings

### Database Configuration

The application requires a MySQL database with the following schema:
- Tables: users, roles, customers, contracts, categories, pdfs
- Character Set: UTF-8
- Collation: utf8_general_ci

**Database Initialization:**
The application uses `spring.jpa.hibernate.ddl-auto=create-drop` which will:
- Create tables on startup
- Drop tables on shutdown
- **WARNING:** This is suitable for development only. For production, use `validate` or `none`.

---

## Monitoring and Logging

### CloudWatch Logs

**Log Group:** `/ecs/crm`
**Log Stream Prefix:** `ecs`

**View Logs:**
```bash
# Tail logs in real-time
aws logs tail /ecs/crm --follow --region us-east-1

# View specific log stream
aws logs get-log-events \
  --log-group-name /ecs/crm \
  --log-stream-name ecs/crm/<task-id> \
  --region us-east-1
```

### Health Checks

**Application Health:**
- **Endpoint:** http://localhost:8080/actuator/health
- **Expected Response:** `{"status":"UP"}`

**ECS Service Health:**
```bash
aws ecs describe-services \
  --cluster <cluster-name> \
  --services crm-service \
  --region us-east-1
```

### Metrics and Monitoring

**CloudWatch Metrics:**
- CPU Utilization
- Memory Utilization
- Network In/Out
- Task Count

**Custom Metrics:**
- Application-specific metrics via Spring Boot Actuator
- JVM metrics (heap usage, garbage collection)

---

## Troubleshooting

### Common Issues

#### 1. Task Fails to Start

**Symptoms:**
- Tasks start and immediately stop
- "Essential container exited" error

**Solutions:**
- Check CloudWatch logs for application errors
- Verify database connectivity
- Ensure environment variables are correct
- Check IAM role permissions

```bash
# View task stopped reason
aws ecs describe-tasks \
  --cluster <cluster-name> \
  --tasks <task-id> \
  --region us-east-1 \
  --query 'tasks[0].stoppedReason'
```

#### 2. Health Check Failures

**Symptoms:**
- Tasks fail health checks
- Service shows unhealthy targets

**Solutions:**
- Verify health endpoint is accessible: `/actuator/health`
- Check security group allows traffic on port 8080
- Increase health check grace period (currently 300s)
- Review application startup time

#### 3. Database Connection Issues

**Symptoms:**
- Application logs show connection errors
- Tasks restart repeatedly

**Solutions:**
- Verify database host and port are correct
- Check security group allows traffic from ECS tasks to database
- Verify database credentials
- Ensure database is accessible from VPC subnets

```bash
# Test database connectivity from ECS task
aws ecs execute-command \
  --cluster <cluster-name> \
  --task <task-id> \
  --container crm \
  --interactive \
  --command "/bin/sh"
```

#### 4. Out of Memory Errors

**Symptoms:**
- Tasks killed with exit code 137
- "OutOfMemoryError" in logs

**Solutions:**
- Increase task memory allocation
- Adjust JVM heap size in JAVA_OPTS
- Review application memory usage patterns

```env
# Increase heap size
JAVA_OPTS=-Xmx768m -Xms384m -XX:MaxRAMPercentage=75.0
```

#### 5. Image Pull Errors

**Symptoms:**
- "CannotPullContainerError" in task events
- Tasks fail to start

**Solutions:**
- Verify ECR repository exists and image is pushed
- Check ecsTaskExecutionRole has ECR permissions
- Ensure image URI is correct in task definition

```bash
# Verify image exists in ECR
aws ecr describe-images \
  --repository-name crm \
  --region us-east-1
```

### Debugging Commands

**View Service Events:**
```bash
aws ecs describe-services \
  --cluster <cluster-name> \
  --services crm-service \
  --region us-east-1 \
  --query 'services[0].events[0:10]'
```

**View Task Details:**
```bash
aws ecs describe-tasks \
  --cluster <cluster-name> \
  --tasks <task-id> \
  --region us-east-1
```

**View Container Logs:**
```bash
aws logs tail /ecs/crm --follow --region us-east-1
```

---

## Security Considerations

### Container Security

1. **Non-root User:**
   - Application runs as `appuser` (non-root)
   - Reduces attack surface

2. **Minimal Base Image:**
   - Uses Eclipse Temurin JRE (not JDK) for runtime
   - Smaller image size, fewer vulnerabilities

3. **No Unnecessary Tools:**
   - Runtime image contains only Java and application
   - No curl, wget, or other debugging tools

### Network Security

1. **Security Groups:**
   - Restrict inbound traffic to necessary ports only
   - Use separate security groups for ALB and ECS tasks

2. **Private Subnets:**
   - Consider deploying ECS tasks in private subnets
   - Use NAT Gateway for outbound internet access

3. **Database Security:**
   - Use RDS with encryption at rest
   - Store credentials in AWS Secrets Manager
   - Use IAM database authentication if possible

### Secrets Management

**AWS Secrets Manager Integration:**
```json
{
  "secrets": [
    {
      "name": "DB_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:region:account:secret:crm/db-password"
    }
  ]
}
```

**Best Practices:**
- Never hardcode credentials in task definitions
- Use AWS Secrets Manager or Parameter Store
- Rotate credentials regularly
- Use IAM roles for AWS service access

### IAM Permissions

**Minimum Required Permissions:**
- ECR: Pull images
- CloudWatch Logs: Create log streams, put log events
- ECS: Register task definitions, create/update services

**Task Role Permissions:**
- Grant only necessary permissions for application functionality
- Use separate roles for different environments

---

## Scaling and Performance

### Auto Scaling

**Service Auto Scaling:**
```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/<cluster-name>/crm-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10

# Create scaling policy
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/<cluster-name>/crm-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{
    "TargetValue": 70.0,
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
    }
  }'
```

### Performance Tuning

**JVM Tuning:**
```env
# For 1 GB memory allocation
JAVA_OPTS=-Xmx768m -Xms384m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

**Spring Boot Tuning:**
```properties
# Connection pool settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Tomcat settings
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10
```

---

## Maintenance and Updates

### Updating the Application

1. **Build new image:**
   ```bash
   ./scripts/build-push.sh
   ```

2. **Deploy new version:**
   ```bash
   ./scripts/deploy-image.sh
   ```

3. **Monitor deployment:**
   ```bash
   aws ecs describe-services \
     --cluster <cluster-name> \
     --services crm-service \
     --region us-east-1
   ```

### Rolling Back

```bash
# List task definition revisions
aws ecs list-task-definitions \
  --family-prefix crm-task \
  --region us-east-1

# Update service to previous revision
aws ecs update-service \
  --cluster <cluster-name> \
  --service crm-service \
  --task-definition crm-task:<previous-revision> \
  --region us-east-1
```

### Blue/Green Deployment

For zero-downtime deployments, consider using AWS CodeDeploy with ECS:
- Create new task definition
- Deploy to new target group
- Shift traffic gradually
- Rollback if issues detected

---

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/1.5.x/reference/html/)
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [Docker Documentation](https://docs.docker.com/)
- [AWS Fargate Documentation](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html)

---

## Support and Contact

For issues or questions:
1. Check CloudWatch logs for application errors
2. Review this deployment guide
3. Consult AWS ECS troubleshooting documentation
4. Contact your DevOps team or AWS support

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Application:** CRM Spring Boot Application  
**Target Platform:** AWS ECS Fargate
