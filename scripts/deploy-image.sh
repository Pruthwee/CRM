#!/bin/bash

# Deploy to AWS ECS Fargate Script for CRM Application
# This script deploys the Docker image to AWS ECS Fargate

set -e
set -o pipefail

echo "=========================================="
echo "CRM Application - ECS Fargate Deployment"
echo "=========================================="
echo ""

# Project configuration
PROJECT_NAME="crm"
TASK_FAMILY="crm-task"
SERVICE_NAME="crm-service"

echo "=== AWS Configuration ==="
read -p "Enter AWS Region (e.g., us-east-1): " AWS_REGION
read -p "Enter ECS Cluster Name: " CLUSTER_NAME

echo ""
echo "=== Network Configuration ==="
read -p "Enter VPC ID: " VPC_ID
read -p "Enter Subnet IDs (comma-separated, at least 2): " SUBNETS_INPUT
read -p "Enter Security Group ID: " SECURITY_GROUP

# Parse subnets
IFS=',' read -ra SUBNETS <<< "$SUBNETS_INPUT"
SUBNET_1=$(echo "${SUBNETS[0]}" | xargs)
SUBNET_2=$(echo "${SUBNETS[1]}" | xargs)

echo ""
echo "=== Database Configuration ==="
read -p "Enter Database Host: " DB_HOST
read -p "Enter Database Port [3306]: " DB_PORT
DB_PORT=${DB_PORT:-3306}
read -p "Enter Database Name [crm]: " DB_NAME
DB_NAME=${DB_NAME:-crm}
read -p "Enter Database Username: " DB_USERNAME
read -sp "Enter Database Password: " DB_PASSWORD
echo ""

echo ""
echo "=== Container Image Configuration ==="
read -p "Enter ECR Image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/crm:latest): " IMAGE_URI

echo ""
echo "=== Load Balancer Configuration ==="
read -p "Do you need a load balancer for this service? (y/n): " NEED_LB

# Get AWS Account ID
echo ""
echo "Retrieving AWS Account ID..."
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "AWS Account ID: $ACCOUNT_ID"

# Check if ECS cluster exists, create if it doesn't
echo ""
echo "Checking if ECS cluster exists..."
aws ecs describe-clusters --clusters "$CLUSTER_NAME" --region "$AWS_REGION" >/dev/null 2>&1 || {
    echo "Cluster does not exist. Creating ECS cluster: $CLUSTER_NAME"
    aws ecs create-cluster --cluster-name "$CLUSTER_NAME" --region "$AWS_REGION"
    echo "ECS cluster created successfully"
}

# Create CloudWatch log group if it doesn't exist
echo ""
echo "Creating CloudWatch log group..."
aws logs create-log-group --log-group-name "/ecs/$PROJECT_NAME" --region "$AWS_REGION" 2>/dev/null || echo "Log group already exists"

# Handle Load Balancer
TARGET_GROUP_ARN=""
if [[ "$NEED_LB" == "y" || "$NEED_LB" == "Y" ]]; then
    echo ""
    echo "=== Creating Application Load Balancer ==="
    
    # Create ALB
    ALB_NAME="crm-alb"
    echo "Creating Application Load Balancer: $ALB_NAME"
    ALB_ARN=$(aws elbv2 create-load-balancer \
        --name "$ALB_NAME" \
        --subnets "$SUBNET_1" "$SUBNET_2" \
        --security-groups "$SECURITY_GROUP" \
        --scheme internet-facing \
        --type application \
        --ip-address-type ipv4 \
        --region "$AWS_REGION" \
        --query 'LoadBalancers[0].LoadBalancerArn' \
        --output text)
    
    echo "ALB created: $ALB_ARN"
    
    # Create Target Group with target-type ip (required for Fargate)
    TG_NAME="crm-tg"
    echo "Creating Target Group: $TG_NAME"
    TARGET_GROUP_ARN=$(aws elbv2 create-target-group \
        --name "$TG_NAME" \
        --protocol HTTP \
        --port 8080 \
        --vpc-id "$VPC_ID" \
        --target-type ip \
        --health-check-enabled \
        --health-check-protocol HTTP \
        --health-check-path "/actuator/health" \
        --health-check-interval-seconds 30 \
        --health-check-timeout-seconds 5 \
        --healthy-threshold-count 2 \
        --unhealthy-threshold-count 3 \
        --region "$AWS_REGION" \
        --query 'TargetGroups[0].TargetGroupArn' \
        --output text)
    
    echo "Target Group created: $TARGET_GROUP_ARN"
    
    # Create Listener
    echo "Creating ALB Listener..."
    aws elbv2 create-listener \
        --load-balancer-arn "$ALB_ARN" \
        --protocol HTTP \
        --port 80 \
        --default-actions Type=forward,TargetGroupArn="$TARGET_GROUP_ARN" \
        --region "$AWS_REGION" >/dev/null
    
    echo "ALB Listener created"
    
    # Get ALB DNS name
    ALB_DNS=$(aws elbv2 describe-load-balancers \
        --load-balancer-arns "$ALB_ARN" \
        --region "$AWS_REGION" \
        --query 'LoadBalancers[0].DNSName' \
        --output text)
    
    echo "ALB DNS Name: $ALB_DNS"
fi

# Prepare task definition JSON
echo ""
echo "Preparing task definition..."
TASK_DEF_FILE="../ecs/task-definition.json"

# Create temporary task definition with replaced placeholders
TEMP_TASK_DEF=$(mktemp)
sed "s|{{ACCOUNT_ID}}|$ACCOUNT_ID|g; \
     s|{{IMAGE_URI}}|$IMAGE_URI|g; \
     s|{{AWS_REGION}}|$AWS_REGION|g; \
     s|{{DB_HOST}}|$DB_HOST|g; \
     s|{{DB_PORT}}|$DB_PORT|g; \
     s|{{DB_NAME}}|$DB_NAME|g; \
     s|{{DB_USERNAME}}|$DB_USERNAME|g; \
     s|{{DB_PASSWORD}}|$DB_PASSWORD|g" "$TASK_DEF_FILE" > "$TEMP_TASK_DEF"

# Register task definition
echo "Registering task definition..."
TASK_DEF_ARN=$(aws ecs register-task-definition \
    --cli-input-json file://"$TEMP_TASK_DEF" \
    --region "$AWS_REGION" \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

echo "Task definition registered: $TASK_DEF_ARN"

# Clean up temporary file
rm -f "$TEMP_TASK_DEF"

# Prepare service definition JSON
echo ""
echo "Preparing service definition..."
SERVICE_DEF_FILE="../ecs/service-definition.json"

# Create temporary service definition with replaced placeholders
TEMP_SERVICE_DEF=$(mktemp)

if [[ "$NEED_LB" == "y" || "$NEED_LB" == "Y" ]]; then
    # Include load balancer configuration
    sed "s|{{CLUSTER_NAME}}|$CLUSTER_NAME|g; \
         s|{{SUBNET_1}}|$SUBNET_1|g; \
         s|{{SUBNET_2}}|$SUBNET_2|g; \
         s|{{SECURITY_GROUP}}|$SECURITY_GROUP|g; \
         s|{{TARGET_GROUP_ARN}}|$TARGET_GROUP_ARN|g" "$SERVICE_DEF_FILE" > "$TEMP_SERVICE_DEF"
else
    # Remove load balancer configuration
    sed "s|{{CLUSTER_NAME}}|$CLUSTER_NAME|g; \
         s|{{SUBNET_1}}|$SUBNET_1|g; \
         s|{{SUBNET_2}}|$SUBNET_2|g; \
         s|{{SECURITY_GROUP}}|$SECURITY_GROUP|g" "$SERVICE_DEF_FILE" | \
    jq 'del(.loadBalancers) | del(.healthCheckGracePeriodSeconds)' > "$TEMP_SERVICE_DEF"
fi

# Check if service exists
echo "Checking if service exists..."
SERVICE_EXISTS=$(aws ecs describe-services \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION" \
    --query 'services[?status==`ACTIVE`].serviceName' \
    --output text)

if [ -z "$SERVICE_EXISTS" ]; then
    # Create new service
    echo "Creating new ECS service: $SERVICE_NAME"
    aws ecs create-service \
        --cli-input-json file://"$TEMP_SERVICE_DEF" \
        --region "$AWS_REGION" >/dev/null
    
    echo "ECS service created successfully"
else
    # Update existing service
    echo "Updating existing ECS service: $SERVICE_NAME"
    aws ecs update-service \
        --cluster "$CLUSTER_NAME" \
        --service "$SERVICE_NAME" \
        --task-definition "$TASK_DEF_ARN" \
        --desired-count 2 \
        --region "$AWS_REGION" >/dev/null
    
    echo "ECS service updated successfully"
fi

# Clean up temporary file
rm -f "$TEMP_SERVICE_DEF"

# Wait for service to become stable
echo ""
echo "Waiting for service to become stable (this may take a few minutes)..."
aws ecs wait services-stable \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION"

echo ""
echo "=========================================="
echo "Deployment Completed Successfully"
echo "=========================================="
echo ""

# Display service information
echo "Service Details:"
aws ecs describe-services \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION" \
    --query 'services[0].[serviceName,status,runningCount,desiredCount]' \
    --output table

echo ""
echo "CloudWatch Logs:"
echo "  Log Group: /ecs/$PROJECT_NAME"
echo "  Region: $AWS_REGION"
echo ""

if [[ "$NEED_LB" == "y" || "$NEED_LB" == "Y" ]]; then
    echo "Application URL:"
    echo "  http://$ALB_DNS"
    echo ""
fi

echo "To view logs:"
echo "  aws logs tail /ecs/$PROJECT_NAME --follow --region $AWS_REGION"
echo ""
echo "To check service status:"
echo "  aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --region $AWS_REGION"
echo ""
