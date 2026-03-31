# AWS Deployment Guide - Cloud-Native CRM Application

## Overview
This guide provides step-by-step instructions for deploying the cloud-ready CRM application to AWS with distributed session management using ElastiCache Redis.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        AWS Cloud                             │
│                                                              │
│  ┌──────────────┐         ┌──────────────────────┐         │
│  │              │         │   Application Load    │         │
│  │   Route 53   │────────▶│      Balancer        │         │
│  │              │         │                      │         │
│  └──────────────┘         └──────────┬───────────┘         │
│                                      │                      │
│                          ┌───────────┴───────────┐         │
│                          │                       │         │
│                   ┌──────▼──────┐         ┌──────▼──────┐  │
│                   │   ECS Task  │         │   ECS Task  │  │
│                   │  (Instance) │         │  (Instance) │  │
│                   │             │         │             │  │
│                   │  CRM App    │         │  CRM App    │  │
│                   └──────┬──────┘         └──────┬──────┘  │
│                          │                       │         │
│                          └───────────┬───────────┘         │
│                                      │                      │
│                          ┌───────────▼───────────┐         │
│                          │                       │         │
│                   ┌──────▼──────┐         ┌──────▼──────┐  │
│                   │  ElastiCache│         │   RDS MySQL │  │
│                   │    Redis    │         │             │  │
│                   │  (Sessions) │         │  (Database) │  │
│                   └─────────────┘         └─────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Prerequisites

1. **AWS Account** with appropriate permissions
2. **AWS CLI** installed and configured
3. **Docker** installed (for building container images)
4. **ECR Repository** for storing Docker images

## Step 1: Create VPC and Networking

### 1.1 Create VPC
```bash
aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=crm-vpc}]'

# Note the VPC ID
export VPC_ID=<vpc-id>
```

### 1.2 Create Subnets
```bash
# Public Subnet 1 (us-east-1a)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-public-1a}]'

# Public Subnet 2 (us-east-1b)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-public-1b}]'

# Private Subnet 1 (us-east-1a)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.11.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-private-1a}]'

# Private Subnet 2 (us-east-1b)
aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.12.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=crm-private-1b}]'
```

### 1.3 Create Internet Gateway
```bash
aws ec2 create-internet-gateway \
  --tag-specifications 'ResourceType=internet-gateway,Tags=[{Key=Name,Value=crm-igw}]'

export IGW_ID=<igw-id>

aws ec2 attach-internet-gateway \
  --vpc-id $VPC_ID \
  --internet-gateway-id $IGW_ID
```

## Step 2: Create Security Groups

### 2.1 Application Load Balancer Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-alb-sg \
  --description "Security group for CRM ALB" \
  --vpc-id $VPC_ID

export ALB_SG_ID=<sg-id>

# Allow HTTP/HTTPS from internet
aws ec2 authorize-security-group-ingress \
  --group-id $ALB_SG_ID \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
  --group-id $ALB_SG_ID \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0
```

### 2.2 Application Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-app-sg \
  --description "Security group for CRM application" \
  --vpc-id $VPC_ID

export APP_SG_ID=<sg-id>

# Allow traffic from ALB
aws ec2 authorize-security-group-ingress \
  --group-id $APP_SG_ID \
  --protocol tcp \
  --port 8080 \
  --source-group $ALB_SG_ID
```

### 2.3 Redis Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-redis-sg \
  --description "Security group for Redis" \
  --vpc-id $VPC_ID

export REDIS_SG_ID=<sg-id>

# Allow Redis from application
aws ec2 authorize-security-group-ingress \
  --group-id $REDIS_SG_ID \
  --protocol tcp \
  --port 6379 \
  --source-group $APP_SG_ID
```

### 2.4 RDS Security Group
```bash
aws ec2 create-security-group \
  --group-name crm-rds-sg \
  --description "Security group for RDS MySQL" \
  --vpc-id $VPC_ID

export RDS_SG_ID=<sg-id>

# Allow MySQL from application
aws ec2 authorize-security-group-ingress \
  --group-id $RDS_SG_ID \
  --protocol tcp \
  --port 3306 \
  --source-group $APP_SG_ID
```

## Step 3: Create ElastiCache Redis Cluster

### 3.1 Create Subnet Group
```bash
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name crm-redis-subnet-group \
  --cache-subnet-group-description "Subnet group for CRM Redis" \
  --subnet-ids subnet-xxx subnet-yyy
```

### 3.2 Create Redis Cluster
```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id crm-session-store \
  --engine redis \
  --cache-node-type cache.t3.micro \
  --num-cache-nodes 1 \
  --engine-version 6.x \
  --cache-subnet-group-name crm-redis-subnet-group \
  --security-group-ids $REDIS_SG_ID \
  --tags Key=Name,Value=crm-redis Key=Environment,Value=production
```

### 3.3 Get Redis Endpoint
```bash
aws elasticache describe-cache-clusters \
  --cache-cluster-id crm-session-store \
  --show-cache-node-info

# Note the endpoint address
export REDIS_ENDPOINT=<endpoint-address>
```

## Step 4: Create RDS MySQL Database

### 4.1 Create DB Subnet Group
```bash
aws rds create-db-subnet-group \
  --db-subnet-group-name crm-db-subnet-group \
  --db-subnet-group-description "Subnet group for CRM database" \
  --subnet-ids subnet-xxx subnet-yyy
```

### 4.2 Create RDS Instance
```bash
aws rds create-db-instance \
  --db-instance-identifier crm-database \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 5.7 \
  --master-username admin \
  --master-user-password <secure-password> \
  --allocated-storage 20 \
  --db-subnet-group-name crm-db-subnet-group \
  --vpc-security-group-ids $RDS_SG_ID \
  --backup-retention-period 7 \
  --preferred-backup-window "03:00-04:00" \
  --preferred-maintenance-window "mon:04:00-mon:05:00" \
  --tags Key=Name,Value=crm-database Key=Environment,Value=production
```

### 4.3 Get RDS Endpoint
```bash
aws rds describe-db-instances \
  --db-instance-identifier crm-database

# Note the endpoint address
export RDS_ENDPOINT=<endpoint-address>
```

## Step 5: Store Secrets in AWS Secrets Manager

### 5.1 Create Database Secret
```bash
aws secretsmanager create-secret \
  --name crm/database/credentials \
  --description "CRM database credentials" \
  --secret-string '{
    "username": "admin",
    "password": "<secure-password>",
    "host": "'$RDS_ENDPOINT'",
    "port": 3306,
    "database": "crm"
  }'
```

### 5.2 Create Redis Secret (if auth enabled)
```bash
aws secretsmanager create-secret \
  --name crm/redis/credentials \
  --description "CRM Redis credentials" \
  --secret-string '{
    "host": "'$REDIS_ENDPOINT'",
    "port": 6379,
    "password": ""
  }'
```

## Step 6: Build and Push Docker Image

### 6.1 Create ECR Repository
```bash
aws ecr create-repository \
  --repository-name crm-application \
  --image-scanning-configuration scanOnPush=true

export ECR_REPO_URI=<repository-uri>
```

### 6.2 Build Docker Image
```bash
docker build -t crm-application:latest .
```

### 6.3 Tag and Push to ECR
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_REPO_URI

# Tag image
docker tag crm-application:latest $ECR_REPO_URI:latest

# Push image
docker push $ECR_REPO_URI:latest
```

## Step 7: Create ECS Cluster and Task Definition

### 7.1 Create ECS Cluster
```bash
aws ecs create-cluster \
  --cluster-name crm-cluster \
  --capacity-providers FARGATE FARGATE_SPOT \
  --default-capacity-provider-strategy \
    capacityProvider=FARGATE,weight=1,base=1 \
    capacityProvider=FARGATE_SPOT,weight=4
```

### 7.2 Create Task Execution Role
```bash
# Create trust policy
cat > trust-policy.json <<EOF
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
  --role-name crmEcsTaskExecutionRole \
  --assume-role-policy-document file://trust-policy.json

# Attach policies
aws iam attach-role-policy \
  --role-name crmEcsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

aws iam attach-role-policy \
  --role-name crmEcsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/SecretsManagerReadWrite
```

### 7.3 Create Task Definition
```bash
cat > task-definition.json <<EOF
{
  "family": "crm-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<account-id>:role/crmEcsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "crm-app",
      "image": "$ECR_REPO_URI:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "cloud"
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
          "name": "DATABASE_URL",
          "value": "jdbc:mysql://$RDS_ENDPOINT:3306/crm?useSSL=true"
        }
      ],
      "secrets": [
        {
          "name": "DATABASE_USERNAME",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:<account-id>:secret:crm/database/credentials:username::"
        },
        {
          "name": "DATABASE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:<account-id>:secret:crm/database/credentials:password::"
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
        "command": ["CMD-SHELL", "wget --quiet --tries=1 --spider http://localhost:8080/appinfo/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
EOF

aws ecs register-task-definition --cli-input-json file://task-definition.json
```

## Step 8: Create Application Load Balancer

### 8.1 Create ALB
```bash
aws elbv2 create-load-balancer \
  --name crm-alb \
  --subnets subnet-xxx subnet-yyy \
  --security-groups $ALB_SG_ID \
  --scheme internet-facing \
  --type application \
  --ip-address-type ipv4

export ALB_ARN=<alb-arn>
```

### 8.2 Create Target Group
```bash
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
  --unhealthy-threshold-count 3

export TG_ARN=<target-group-arn>
```

### 8.3 Create Listener
```bash
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN
```

## Step 9: Create ECS Service

```bash
aws ecs create-service \
  --cluster crm-cluster \
  --service-name crm-service \
  --task-definition crm-task \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[subnet-xxx,subnet-yyy],
    securityGroups=[$APP_SG_ID],
    assignPublicIp=DISABLED
  }" \
  --load-balancers "targetGroupArn=$TG_ARN,containerName=crm-app,containerPort=8080" \
  --health-check-grace-period-seconds 60
```

## Step 10: Configure Auto Scaling

### 10.1 Register Scalable Target
```bash
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/crm-cluster/crm-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10
```

### 10.2 Create Scaling Policy
```bash
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/crm-cluster/crm-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name crm-cpu-scaling \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{
    "TargetValue": 70.0,
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
    },
    "ScaleInCooldown": 300,
    "ScaleOutCooldown": 60
  }'
```

## Step 11: Configure CloudWatch Monitoring

### 11.1 Create Log Group
```bash
aws logs create-log-group --log-group-name /ecs/crm-app
aws logs put-retention-policy --log-group-name /ecs/crm-app --retention-in-days 30
```

### 11.2 Create CloudWatch Alarms
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
  --evaluation-periods 2

# Redis high memory alarm
aws cloudwatch put-metric-alarm \
  --alarm-name crm-redis-high-memory \
  --alarm-description "Alert when Redis memory exceeds 80%" \
  --metric-name DatabaseMemoryUsagePercentage \
  --namespace AWS/ElastiCache \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2
```

## Step 12: Verify Deployment

### 12.1 Get ALB DNS Name
```bash
aws elbv2 describe-load-balancers \
  --load-balancer-arns $ALB_ARN \
  --query 'LoadBalancers[0].DNSName' \
  --output text
```

### 12.2 Test Application
```bash
# Health check
curl http://<alb-dns-name>/appinfo/health

# Application
curl http://<alb-dns-name>/
```

### 12.3 Verify Session Sharing
```bash
# Connect to Redis
redis-cli -h $REDIS_ENDPOINT

# List sessions
KEYS spring:session:*

# View session data
GET spring:session:sessions:<session-id>
```

## Maintenance and Operations

### Update Application
```bash
# Build and push new image
docker build -t crm-application:v2 .
docker tag crm-application:v2 $ECR_REPO_URI:v2
docker push $ECR_REPO_URI:v2

# Update task definition with new image
# Update service to use new task definition
aws ecs update-service \
  --cluster crm-cluster \
  --service crm-service \
  --task-definition crm-task:2 \
  --force-new-deployment
```

### Scale Service
```bash
aws ecs update-service \
  --cluster crm-cluster \
  --service crm-service \
  --desired-count 4
```

### View Logs
```bash
aws logs tail /ecs/crm-app --follow
```

## Cost Optimization

1. **Use Fargate Spot** for non-critical workloads (already configured)
2. **Right-size resources** based on CloudWatch metrics
3. **Enable RDS auto-scaling** for storage
4. **Use Reserved Instances** for predictable workloads
5. **Enable S3 lifecycle policies** for log archival

## Security Best Practices

1. ✅ Use Secrets Manager for credentials
2. ✅ Enable encryption at rest (RDS, ElastiCache)
3. ✅ Enable encryption in transit (HTTPS, SSL)
4. ✅ Use VPC with private subnets
5. ✅ Implement least privilege IAM roles
6. ✅ Enable CloudTrail for audit logging
7. ✅ Use WAF for application protection
8. ✅ Enable GuardDuty for threat detection

## Troubleshooting

### Service won't start
- Check CloudWatch logs: `aws logs tail /ecs/crm-app --follow`
- Verify security groups allow traffic
- Check task execution role permissions

### Can't connect to Redis
- Verify security group allows port 6379
- Check Redis endpoint is correct
- Verify VPC and subnet configuration

### Database connection errors
- Verify RDS security group
- Check database credentials in Secrets Manager
- Verify RDS endpoint is correct

## Conclusion

Your CRM application is now deployed to AWS with:
- ✅ Distributed session management (ElastiCache Redis)
- ✅ Horizontal scaling (ECS Fargate with auto-scaling)
- ✅ High availability (multi-AZ deployment)
- ✅ Load balancing (Application Load Balancer)
- ✅ Monitoring and logging (CloudWatch)
- ✅ Security (VPC, security groups, Secrets Manager)
- ✅ Cost optimization (Fargate Spot)

The application is fully cloud-native and ready for production workloads!
