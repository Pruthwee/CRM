# Cloud Deployment Guide - AWS

## Overview
This application has been updated for cloud deployment on AWS with the following improvements:

## Cloud-Ready Features Implemented

### 1. Configuration Management
- All hardcoded credentials removed and externalized to environment variables
- Database credentials use AWS Secrets Manager or environment variables
- Support for AWS RDS MySQL endpoints
- HikariCP connection pooling configured for cloud databases

### 2. File Storage
- PDF generation changed from local file system to in-memory byte arrays
- Ready for AWS S3 integration for persistent storage
- Desktop GUI file chooser replaced with web-based file upload

### 3. Framework Upgrades
- Spring Boot upgraded from 1.5.10 to 2.7.18
- Java upgraded from 8 to 17 for better container support
- Thymeleaf updated to use standard HTML mode (removed legacy mode)

### 4. Security
- Management endpoints security enabled by default
- Ready for AWS IAM role-based authentication

### 5. Containerization
- Dockerfile with multi-stage build for optimized image size
- Non-root user for enhanced security
- Health checks configured
- JVM optimized for containers

## Environment Variables Required

### Database Configuration
```bash
DATABASE_URL=jdbc:mysql://<rds-endpoint>:3306/crm?useSSL=true
DATABASE_USERNAME=<db-username>
DATABASE_PASSWORD=<db-password>
HIBERNATE_DDL_AUTO=validate
```

### AWS Configuration
```bash
AWS_REGION=us-east-1
AWS_S3_ENABLED=true
AWS_S3_BUCKET_NAME=<your-bucket-name>
```

### Application Configuration
```bash
PORT=8080
MANAGEMENT_SECURITY_ENABLED=true
THYMELEAF_CACHE=true
THYMELEAF_MODE=HTML
```

## AWS Deployment Steps

### 1. Build Docker Image
```bash
docker build -t crm-app:latest .
```

### 2. Tag and Push to AWS ECR
```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker tag crm-app:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
```

### 3. Deploy to AWS ECS/Fargate
Create ECS Task Definition with:
- Container image from ECR
- Environment variables configured
- IAM role with S3 and Secrets Manager permissions
- CloudWatch Logs integration

### 4. Configure AWS RDS MySQL
- Create RDS MySQL instance with multi-AZ
- Store credentials in AWS Secrets Manager
- Configure security groups to allow ECS access

### 5. Configure AWS S3
- Create S3 bucket for PDF storage
- Enable server-side encryption
- Configure lifecycle policies

### 6. Configure Application Load Balancer
- Create ALB with health check: /appinfo/health
- Configure target group pointing to ECS service
- Set up SSL/TLS certificate with AWS ACM

## Database Migration
Before deploying, run database migrations using Flyway or Liquibase:
1. Set `HIBERNATE_DDL_AUTO=validate` in production
2. Use database migration tools for schema changes
3. Never use `create-drop` in production

## Monitoring
- CloudWatch Logs: Application logs automatically forwarded
- CloudWatch Metrics: CPU, memory, request metrics
- CloudWatch Alarms: Set up for error rates and performance

## Fixed Issues Summary
1. ✅ Local file system write operations replaced with in-memory processing
2. ✅ Desktop GUI components removed, replaced with web-based alternatives
3. ✅ Database credentials externalized to environment variables
4. ✅ Hardcoded localhost URLs replaced with environment variables
5. ✅ Destructive DDL mode changed to validate
6. ✅ Spring Boot upgraded to 2.7.18
7. ✅ Java upgraded to 17
8. ✅ Management security enabled
9. ✅ Thymeleaf legacy mode removed
10. ✅ Connection pooling configured
11. ✅ AWS SDK dependencies added
