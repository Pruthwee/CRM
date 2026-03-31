#!/bin/bash

# AWS ECS Deployment Script for CRM LiveLog Application
# This script deploys the application to AWS ECS with distributed session management

set -e

# Configuration
AWS_REGION="${AWS_REGION:-us-east-1}"
AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID}"
ECR_REPOSITORY="crm-livelog"
ECS_CLUSTER="crm-cluster"
ECS_SERVICE="crm-livelog-service"
TASK_DEFINITION="crm-livelog-task"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}CRM LiveLog - AWS ECS Deployment${NC}"
echo -e "${GREEN}========================================${NC}"

# Check required environment variables
if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo -e "${RED}Error: AWS_ACCOUNT_ID environment variable is not set${NC}"
    exit 1
fi

# Step 1: Build Docker image
echo -e "\n${YELLOW}Step 1: Building Docker image...${NC}"
docker build -t ${ECR_REPOSITORY}:latest .

# Step 2: Tag image for ECR
echo -e "\n${YELLOW}Step 2: Tagging image for ECR...${NC}"
docker tag ${ECR_REPOSITORY}:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}:latest

# Step 3: Login to ECR
echo -e "\n${YELLOW}Step 3: Logging in to ECR...${NC}"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Step 4: Push image to ECR
echo -e "\n${YELLOW}Step 4: Pushing image to ECR...${NC}"
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}:latest

# Step 5: Update task definition
echo -e "\n${YELLOW}Step 5: Updating ECS task definition...${NC}"
sed -e "s/ACCOUNT_ID/${AWS_ACCOUNT_ID}/g" \
    -e "s/REGION/${AWS_REGION}/g" \
    aws/ecs-task-definition.json > /tmp/ecs-task-definition.json

aws ecs register-task-definition \
    --cli-input-json file:///tmp/ecs-task-definition.json \
    --region ${AWS_REGION}

# Step 6: Update ECS service
echo -e "\n${YELLOW}Step 6: Updating ECS service...${NC}"
aws ecs update-service \
    --cluster ${ECS_CLUSTER} \
    --service ${ECS_SERVICE} \
    --task-definition ${TASK_DEFINITION} \
    --force-new-deployment \
    --region ${AWS_REGION}

# Step 7: Wait for deployment to complete
echo -e "\n${YELLOW}Step 7: Waiting for deployment to complete...${NC}"
aws ecs wait services-stable \
    --cluster ${ECS_CLUSTER} \
    --services ${ECS_SERVICE} \
    --region ${AWS_REGION}

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

# Get service information
echo -e "\n${YELLOW}Service Information:${NC}"
aws ecs describe-services \
    --cluster ${ECS_CLUSTER} \
    --services ${ECS_SERVICE} \
    --region ${AWS_REGION} \
    --query 'services[0].[serviceName,status,runningCount,desiredCount]' \
    --output table

echo -e "\n${GREEN}Application is now running with distributed session management!${NC}"
echo -e "${GREEN}Sessions are stored in ElastiCache Redis for horizontal scaling.${NC}"
