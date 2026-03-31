# AWS Deployment Guide - CRM Application

## Overview
This guide provides step-by-step instructions for deploying the cloud-ready CRM application to AWS using ECS, ElastiCache Redis, and RDS MySQL.

## Prerequisites
- AWS CLI installed and configured
- Docker installed
- Maven installed
- AWS account with appropriate permissions

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     AWS Cloud                                │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │              Application Load Balancer              │    │
│  │                  (Port 80/443)                      │    │
│  └──────────────────┬─────────────────────────────────┘    │
│                     │                                        │
│  ┌──────────────────┴─────────────────────────────────┐    │
│  │              ECS Service (Auto Scaling)             │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │    │
│  │  │ ECS Task │  │ ECS Task │  │ ECS Task │         │    │
│  │  │  (App)   │  │  (App)   │  │  (App)   │         │    │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘         │    │
│  └───────┼─────────────┼─────────────┼───────────────┘    │
│          │             │             │                      │
│  ┌───────┴─────────────┴─────────────┴───────────────┐    │
│  │         ElastiCache Redis Cluster                  │    │
│  │         (Session Store - Shared State)             │    │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              RDS MySQL Instance                      │   │
│  │              (Application Database)                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              AWS Secrets Manager                     │   │
│  │         (DB Password, Redis Password)                │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              CloudWatch Logs                         │   │
│  │         (Application Logs & Metrics)                 │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Step 1: Create VPC and Security Groups

### 1.1 Create VPC (if not using default)
```bash
aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=crm-vpc}]'

# Note the VPC ID from output
export VPC_ID=<vpc-id>
```

### 1.2 Create Subnets
```bash
# Public Subnet 1 (for ALB)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-public-1}]'

export PUBLIC_SUBNET_1=<subnet-id>

# Public Subnet 2 (for ALB)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-public-2}]'

export PUBLIC_SUBNET_2=<subnet-id>

# Private Subnet 1 (for ECS, RDS, Redis)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.10.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-private-1}]'

export PRIVATE_SUBNET_1=<subnet-id>

# Private Subnet 2 (for RDS Multi-AZ)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.11.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-private-2}]'

export PRIVATE_SUBNET_2=<subnet-id>
```

### 1.3 Create Security Groups

#### ALB Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-alb-sg \
  --description "Security group for CRM ALB" \
  --vpc-id $VPC_ID

export ALB_SG=<security-group-id>

# Allow HTTP from internet
aws ec2 authorize-security-group-ingress \
  --group-id $ALB_SG \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0

# Allow HTTPS from internet
aws ec2 authorize-security-group-ingress \
  --group-id $ALB_SG \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0
```

#### ECS Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-ecs-sg \
  --description "Security group for CRM ECS tasks" \
  --vpc-id $VPC_ID

export ECS_SG=<security-group-id>

# Allow traffic from ALB
aws ec2 authorize-security-group-ingress \
  --group-id $ECS_SG \
  --protocol tcp \
  --port 8080 \
  --source-group $ALB_SG
```

#### RDS Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-rds-sg \
  --description "Security group for CRM RDS" \
  --vpc-id $VPC_ID

export RDS_SG=<security-group-id>

# Allow MySQL from ECS
aws ec2 authorize-security-group-ingress \
  --group-id $RDS_SG \
  --protocol tcp \
  --port 3306 \
  --source-group $ECS_SG
```

#### ElastiCache Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-redis-sg \
  --description "Security group for CRM Redis" \
  --vpc-id $VPC_ID

export REDIS_SG=<security-group-id>

# Allow Redis from ECS
aws ec2 authorize-security-group-ingress \
  --group-id $REDIS_SG \
  --protocol tcp \
  --port 6379 \
  --source-group $ECS_SG
```

## Step 2: Create RDS MySQL Database

### 2.1 Create DB Subnet Group
```bash
aws rds create-db-subnet-group \
  --db-subnet-group-name crm-db-subnet-group \
  --db-subnet-group-description "Subnet group for CRM database" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2 \
  --tags Key=Name,Value=crm-db-subnet-group
```

### 2.2 Create RDS Instance
```bash
aws rds create-db-instance \
  --db-instance-identifier crm-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username admin \
  --master-user-password <SECURE_PASSWORD> \
  --allocated-storage 20 \
  --storage-type gp2 \
  --vpc-security-group-ids $RDS_SG \
  --db-subnet-group-name crm-db-subnet-group \
  --backup-retention-period 7 \
  --preferred-backup-window "03:00-04:00" \
  --preferred-maintenance-window "mon:04:00-mon:05:00" \
  --multi-az \
  --publicly-accessible false \
  --tags Key=Name,Value=crm-database

# Wait for database to be available (takes 5-10 minutes)
aws rds wait db-instance-available --db-instance-identifier crm-db

# Get the endpoint
aws rds describe-db-instances \
  --db-instance-identifier crm-db \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text

export DB_ENDPOINT=<endpoint>
```

### 2.3 Create Database Schema
```bash
# Connect to RDS and create database
mysql -h $DB_ENDPOINT -u admin -p

CREATE DATABASE crm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE crm;

# The application will create tables automatically via Hibernate
# with spring.jpa.hibernate.ddl-auto=update
```

## Step 3: Create ElastiCache Redis Cluster

### 3.1 Create Cache Subnet Group
```bash
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name crm-redis-subnet-group \
  --cache-subnet-group-description "Subnet group for CRM Redis" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2
```

### 3.2 Create Redis Cluster
```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id crm-session-cache \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --engine-version 7.0 \
  --num-cache-nodes 1 \
  --cache-subnet-group-name crm-redis-subnet-group \
  --security-group-ids $REDIS_SG \
  --preferred-maintenance-window "sun:05:00-sun:06:00" \
  --tags Key=Name,Value=crm-redis

# Wait for cluster to be available (takes 5-10 minutes)
aws elasticache wait cache-cluster-available --cache-cluster-id crm-session-cache

# Get the endpoint
aws elasticache describe-cache-clusters \
  --cache-cluster-id crm-session-cache \
  --show-cache-node-info \
  --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' \
  --output text

export REDIS_ENDPOINT=<endpoint>
```

## Step 4: Store Secrets in AWS Secrets Manager

### 4.1 Create Database Password Secret
```bash
aws secretsmanager create-secret \
  --name crm/db-password \
  --description "CRM database password" \
  --secret-string "<SECURE_PASSWORD>"

export DB_PASSWORD_ARN=$(aws secretsmanager describe-secret \
  --secret-id crm/db-password \
  --query 'ARN' \
  --output text)
```

### 4.2 Create Redis Password Secret (if using AUTH)
```bash
# If Redis AUTH is enabled
aws secretsmanager create-secret \
  --name crm/redis-password \
  --description "CRM Redis password" \
  --secret-string "<REDIS_PASSWORD>"

export REDIS_PASSWORD_ARN=$(aws secretsmanager describe-secret \
  --secret-id crm/redis-password \
  --query 'ARN' \
  --output text)
```

## Step 5: Build and Push Docker Image

### 5.1 Build Application
```bash
# Navigate to project directory
cd /modernize-data/studio-data/TNT1001/APP608560/transformed-code/205/studio-workspace/clout-rahullog

# Build with Maven
mvn clean package -DskipTests

# Verify JAR is created
ls -lh target/crm-0.0.1-SNAPSHOT.jar
```

### 5.2 Create Dockerfile (if not exists)
```dockerfile
FROM openjdk:8-jre-alpine

# Add application user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy JAR file
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/appinfo/health || exit 1

# Run application
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Xms256m", \
  "-Xmx512m", \
  "-jar", \
  "app.jar"]
```

### 5.3 Create ECR Repository
```bash
aws ecr create-repository \
  --repository-name crm-app \
  --image-scanning-configuration scanOnPush=true \
  --tags Key=Name,Value=crm-app

export ECR_REPO=$(aws ecr describe-repositories \
  --repository-names crm-app \
  --query 'repositories[0].repositoryUri' \
  --output text)
```

### 5.4 Build and Push Docker Image
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_REPO

# Build image
docker build -t crm-app:latest .

# Tag image
docker tag crm-app:latest $ECR_REPO:latest
docker tag crm-app:latest $ECR_REPO:v1.0.0

# Push image
docker push $ECR_REPO:latest
docker push $ECR_REPO:v1.0.0
```

## Step 6: Create ECS Cluster and Service

### 6.1 Create ECS Cluster
```bash
aws ecs create-cluster \
  --cluster-name crm-cluster \
  --capacity-providers FARGATE FARGATE_SPOT \
  --default-capacity-provider-strategy \
    capacityProvider=FARGATE,weight=1,base=1 \
    capacityProvider=FARGATE_SPOT,weight=4 \
  --tags Key=Name,Value=crm-cluster
```

### 6.2 Create IAM Role for ECS Task Execution
```bash
# Create trust policy
cat > ecs-task-execution-trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

# Create role
aws iam create-role \
  --role-name crm-ecs-task-execution-role \
  --assume-role-policy-document file://ecs-task-execution-trust-policy.json

# Attach AWS managed policy
aws iam attach-role-policy \
  --role-name crm-ecs-task-execution-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Create custom policy for Secrets Manager
cat > ecs-secrets-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "$DB_PASSWORD_ARN",
        "$REDIS_PASSWORD_ARN"
      ]
    }
  ]
}
EOF

aws iam put-role-policy \
  --role-name crm-ecs-task-execution-role \
  --policy-name crm-secrets-access \
  --policy-document file://ecs-secrets-policy.json

export TASK_EXECUTION_ROLE_ARN=$(aws iam get-role \
  --role-name crm-ecs-task-execution-role \
  --query 'Role.Arn' \
  --output text)
```

### 6.3 Create CloudWatch Log Group
```bash
aws logs create-log-group \
  --log-group-name /ecs/crm-app \
  --tags Key=Name,Value=crm-app-logs
```

### 6.4 Create ECS Task Definition
```bash
cat > task-definition.json <<EOF
{
  "family": "crm-app",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "$TASK_EXECUTION_ROLE_ARN",
  "containerDefinitions": [
    {
      "name": "crm-app",
      "image": "$ECR_REPO:latest",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "DB_URL",
          "value": "jdbc:mysql://$DB_ENDPOINT:3306/crm?useSSL=false"
        },
        {
          "name": "DB_USERNAME",
          "value": "admin"
        },
        {
          "name": "REDIS_HOST",
          "value": "$REDIS_ENDPOINT"
        },
        {
          "name": "REDIS_PORT",
          "value": "6379"
        },
        {
          "name": "DB_POOL_SIZE",
          "value": "10"
        },
        {
          "name": "LOG_LEVEL_ROOT",
          "value": "INFO"
        },
        {
          "name": "LOG_LEVEL_APP",
          "value": "INFO"
        }
      ],
      "secrets": [
        {
          "name": "DB_PASSWORD",
          "valueFrom": "$DB_PASSWORD_ARN"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/crm-app",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "wget --quiet --tries=1 --spider http://localhost:8080/appinfo/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
EOF

aws ecs register-task-definition \
  --cli-input-json file://task-definition.json
```

### 6.5 Create Application Load Balancer
```bash
# Create ALB
aws elbv2 create-load-balancer \
  --name crm-alb \
  --subnets $PUBLIC_SUBNET_1 $PUBLIC_SUBNET_2 \
  --security-groups $ALB_SG \
  --scheme internet-facing \
  --type application \
  --ip-address-type ipv4 \
  --tags Key=Name,Value=crm-alb

export ALB_ARN=$(aws elbv2 describe-load-balancers \
  --names crm-alb \
  --query 'LoadBalancers[0].LoadBalancerArn' \
  --output text)

export ALB_DNS=$(aws elbv2 describe-load-balancers \
  --names crm-alb \
  --query 'LoadBalancers[0].DNSName' \
  --output text)

# Create Target Group
aws elbv2 create-target-group \
  --name crm-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id $VPC_ID \
  --target-type ip \
  --health-check-enabled \
  --health-check-protocol HTTP \
  --health-check-path /appinfo/health \
  --health-check-interval-seconds 30 \
  --health-check-timeout-seconds 5 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3 \
  --tags Key=Name,Value=crm-target-group

export TG_ARN=$(aws elbv2 describe-target-groups \
  --names crm-tg \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text)

# Create Listener
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN
```

### 6.6 Create ECS Service
```bash
aws ecs create-service \
  --cluster crm-cluster \
  --service-name crm-service \
  --task-definition crm-app \
  --desired-count 2 \
  --launch-type FARGATE \
  --platform-version LATEST \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=$TG_ARN,containerName=crm-app,containerPort=8080" \
  --health-check-grace-period-seconds 60 \
  --tags Key=Name,Value=crm-service

# Wait for service to stabilize
aws ecs wait services-stable \
  --cluster crm-cluster \
  --services crm-service
```

## Step 7: Configure Auto Scaling

### 7.1 Register Scalable Target
```bash
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --scalable-dimension ecs:service:DesiredCount \
  --resource-id service/crm-cluster/crm-service \
  --min-capacity 2 \
  --max-capacity 10
```

### 7.2 Create Scaling Policy
```bash
# CPU-based scaling
cat > scaling-policy.json <<EOF
{
  "TargetValue": 70.0,
  "PredefinedMetricSpecification": {
    "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
  },
  "ScaleInCooldown": 300,
  "ScaleOutCooldown": 60
}
EOF

aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --scalable-dimension ecs:service:DesiredCount \
  --resource-id service/crm-cluster/crm-service \
  --policy-name crm-cpu-scaling \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration file://scaling-policy.json
```

## Step 8: Verify Deployment

### 8.1 Check Service Status
```bash
aws ecs describe-services \
  --cluster crm-cluster \
  --services crm-service \
  --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount}'
```

### 8.2 Check Task Health
```bash
aws ecs list-tasks \
  --cluster crm-cluster \
  --service-name crm-service

# Get task details
aws ecs describe-tasks \
  --cluster crm-cluster \
  --tasks <task-arn>
```

### 8.3 Test Application
```bash
# Get ALB DNS name
echo "Application URL: http://$ALB_DNS"

# Test health endpoint
curl http://$ALB_DNS/appinfo/health

# Test application
curl http://$ALB_DNS/
```

### 8.4 Verify Session Distribution
```bash
# Login to application
# Make multiple requests
# Verify session is maintained across different tasks

# Check Redis for session data
redis-cli -h $REDIS_ENDPOINT
> KEYS crm:session:*
> GET crm:session:sessions:<session-id>
```

## Step 9: Monitoring and Logging

### 9.1 View Application Logs
```bash
# View logs in CloudWatch
aws logs tail /ecs/crm-app --follow

# Filter logs
aws logs filter-log-events \
  --log-group-name /ecs/crm-app \
  --filter-pattern "ERROR"
```

### 9.2 Create CloudWatch Dashboard
```bash
# Create dashboard for monitoring
aws cloudwatch put-dashboard \
  --dashboard-name crm-app-dashboard \
  --dashboard-body file://dashboard.json
```

### 9.3 Set Up Alarms
```bash
# High CPU alarm
aws cloudwatch put-metric-alarm \
  --alarm-name crm-high-cpu \
  --alarm-description "Alert when CPU exceeds 80%" \
  --metric-name CPUUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2 \
  --dimensions Name=ServiceName,Value=crm-service Name=ClusterName,Value=crm-cluster

# High memory alarm
aws cloudwatch put-metric-alarm \
  --alarm-name crm-high-memory \
  --alarm-description "Alert when memory exceeds 80%" \
  --metric-name MemoryUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2 \
  --dimensions Name=ServiceName,Value=crm-service Name=ClusterName,Value=crm-cluster
```

## Step 10: Continuous Deployment

### 10.1 Update Application
```bash
# Build new version
mvn clean package -DskipTests

# Build and push new image
docker build -t crm-app:v1.0.1 .
docker tag crm-app:v1.0.1 $ECR_REPO:v1.0.1
docker tag crm-app:v1.0.1 $ECR_REPO:latest
docker push $ECR_REPO:v1.0.1
docker push $ECR_REPO:latest

# Update service (triggers rolling deployment)
aws ecs update-service \
  --cluster crm-cluster \
  --service crm-service \
  --force-new-deployment

# Monitor deployment
aws ecs wait services-stable \
  --cluster crm-cluster \
  --services crm-service
```

## Cost Optimization

### Estimated Monthly Costs (us-east-1)
- **ECS Fargate** (2 tasks, 0.5 vCPU, 1GB RAM): ~$30/month
- **RDS MySQL** (db.t3.micro, Multi-AZ): ~$30/month
- **ElastiCache Redis** (cache.t3.micro): ~$15/month
- **Application Load Balancer**: ~$20/month
- **Data Transfer**: ~$10/month
- **CloudWatch Logs**: ~$5/month

**Total**: ~$110/month

### Cost Optimization Tips
1. Use Fargate Spot for non-production environments (70% savings)
2. Use RDS Reserved Instances for production (40% savings)
3. Enable RDS storage auto-scaling
4. Use CloudWatch Logs retention policies
5. Implement proper auto-scaling to avoid over-provisioning

## Troubleshooting

### Issue: Tasks failing health checks
```bash
# Check task logs
aws logs tail /ecs/crm-app --follow

# Check task details
aws ecs describe-tasks --cluster crm-cluster --tasks <task-arn>

# Common causes:
# - Database connection issues
# - Redis connection issues
# - Incorrect environment variables
# - Security group misconfiguration
```

### Issue: Cannot connect to database
```bash
# Verify security group allows traffic from ECS
aws ec2 describe-security-groups --group-ids $RDS_SG

# Test connection from ECS task
aws ecs execute-command \
  --cluster crm-cluster \
  --task <task-arn> \
  --container crm-app \
  --interactive \
  --command "/bin/sh"

# Inside container:
nc -zv $DB_ENDPOINT 3306
```

### Issue: Session not persisting
```bash
# Check Redis connectivity
aws ecs execute-command \
  --cluster crm-cluster \
  --task <task-arn> \
  --container crm-app \
  --interactive \
  --command "/bin/sh"

# Inside container:
nc -zv $REDIS_ENDPOINT 6379

# Check Redis logs
aws elasticache describe-events \
  --source-identifier crm-session-cache
```

## Security Best Practices

1. ✅ Use AWS Secrets Manager for sensitive data
2. ✅ Enable encryption at rest for RDS and ElastiCache
3. ✅ Use VPC with private subnets for application tier
4. ✅ Implement least privilege IAM policies
5. ✅ Enable CloudTrail for audit logging
6. ✅ Use AWS WAF with ALB for web application firewall
7. ✅ Enable VPC Flow Logs for network monitoring
8. ✅ Implement regular security patching
9. ✅ Use container image scanning in ECR
10. ✅ Enable AWS GuardDuty for threat detection

## Cleanup (Development/Testing)

```bash
# Delete ECS service
aws ecs update-service --cluster crm-cluster --service crm-service --desired-count 0
aws ecs delete-service --cluster crm-cluster --service crm-service --force

# Delete ECS cluster
aws ecs delete-cluster --cluster crm-cluster

# Delete ALB
aws elbv2 delete-load-balancer --load-balancer-arn $ALB_ARN
aws elbv2 delete-target-group --target-group-arn $TG_ARN

# Delete ElastiCache
aws elasticache delete-cache-cluster --cache-cluster-id crm-session-cache

# Delete RDS
aws rds delete-db-instance --db-instance-identifier crm-db --skip-final-snapshot

# Delete ECR repository
aws ecr delete-repository --repository-name crm-app --force

# Delete secrets
aws secretsmanager delete-secret --secret-id crm/db-password --force-delete-without-recovery
aws secretsmanager delete-secret --secret-id crm/redis-password --force-delete-without-recovery

# Delete security groups, subnets, VPC (in reverse order)
```

## Next Steps

1. ✅ Set up custom domain with Route 53
2. ✅ Configure SSL/TLS certificate with ACM
3. ✅ Implement CI/CD pipeline with CodePipeline
4. ✅ Set up backup and disaster recovery
5. ✅ Implement monitoring and alerting
6. ✅ Configure WAF rules for security
7. ✅ Set up multi-region deployment for HA
8. ✅ Implement blue/green deployment strategy

## Support

For issues or questions:
- Check CloudWatch Logs: `/ecs/crm-app`
- Review ECS service events
- Check application health endpoint: `/appinfo/health`
- Review this guide's troubleshooting section

---

**Deployment Guide Version**: 1.0  
**Last Updated**: 2025-01-29  
**AWS Region**: us-east-1  
**Application Version**: 1.0.0
