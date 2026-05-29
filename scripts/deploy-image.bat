@echo off
setlocal enabledelayedexpansion

REM Deploy to AWS ECS Fargate Script for CRM Application
REM This script deploys the Docker image to AWS ECS Fargate

echo ==========================================
echo CRM Application - ECS Fargate Deployment
echo ==========================================
echo.

REM Project configuration
set PROJECT_NAME=crm
set TASK_FAMILY=crm-task
set SERVICE_NAME=crm-service

echo === AWS Configuration ===
set /p AWS_REGION="Enter AWS Region (e.g., us-east-1): "
set /p CLUSTER_NAME="Enter ECS Cluster Name: "

echo.
echo === Network Configuration ===
set /p VPC_ID="Enter VPC ID: "
set /p SUBNETS_INPUT="Enter Subnet IDs (comma-separated, at least 2): "
set /p SECURITY_GROUP="Enter Security Group ID: "

REM Parse subnets
for /f "tokens=1,2 delims=," %%a in ("%SUBNETS_INPUT%") do (
    set SUBNET_1=%%a
    set SUBNET_2=%%b
)
set SUBNET_1=!SUBNET_1: =!
set SUBNET_2=!SUBNET_2: =!

echo.
echo === Database Configuration ===
set /p DB_HOST="Enter Database Host: "
set /p DB_PORT="Enter Database Port [3306]: "
if "!DB_PORT!"=="" set DB_PORT=3306
set /p DB_NAME="Enter Database Name [crm]: "
if "!DB_NAME!"=="" set DB_NAME=crm
set /p DB_USERNAME="Enter Database Username: "
set /p DB_PASSWORD="Enter Database Password: "

echo.
echo === Container Image Configuration ===
set /p IMAGE_URI="Enter ECR Image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/crm:latest): "

echo.
echo === Load Balancer Configuration ===
set /p NEED_LB="Do you need a load balancer for this service? (y/n): "

REM Get AWS Account ID
echo.
echo Retrieving AWS Account ID...
for /f "tokens=*" %%i in ('aws sts get-caller-identity --query Account --output text') do set ACCOUNT_ID=%%i
echo AWS Account ID: !ACCOUNT_ID!

REM Check if ECS cluster exists, create if it doesn't
echo.
echo Checking if ECS cluster exists...
aws ecs describe-clusters --clusters "!CLUSTER_NAME!" --region "!AWS_REGION!" >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo Cluster does not exist. Creating ECS cluster: !CLUSTER_NAME!
    aws ecs create-cluster --cluster-name "!CLUSTER_NAME!" --region "!AWS_REGION!"
    echo ECS cluster created successfully
)

REM Create CloudWatch log group if it doesn't exist
echo.
echo Creating CloudWatch log group...
aws logs create-log-group --log-group-name "/ecs/!PROJECT_NAME!" --region "!AWS_REGION!" 2>nul
if !ERRORLEVEL! neq 0 (
    echo Log group already exists
)

REM Handle Load Balancer
set TARGET_GROUP_ARN=
if /i "!NEED_LB!"=="y" (
    echo.
    echo === Creating Application Load Balancer ===
    
    REM Create ALB
    set ALB_NAME=crm-alb
    echo Creating Application Load Balancer: !ALB_NAME!
    for /f "tokens=*" %%i in ('aws elbv2 create-load-balancer --name "!ALB_NAME!" --subnets "!SUBNET_1!" "!SUBNET_2!" --security-groups "!SECURITY_GROUP!" --scheme internet-facing --type application --ip-address-type ipv4 --region "!AWS_REGION!" --query "LoadBalancers[0].LoadBalancerArn" --output text') do set ALB_ARN=%%i
    
    echo ALB created: !ALB_ARN!
    
    REM Create Target Group with target-type ip (required for Fargate)
    set TG_NAME=crm-tg
    echo Creating Target Group: !TG_NAME!
    for /f "tokens=*" %%i in ('aws elbv2 create-target-group --name "!TG_NAME!" --protocol HTTP --port 8080 --vpc-id "!VPC_ID!" --target-type ip --health-check-enabled --health-check-protocol HTTP --health-check-path "/actuator/health" --health-check-interval-seconds 30 --health-check-timeout-seconds 5 --healthy-threshold-count 2 --unhealthy-threshold-count 3 --region "!AWS_REGION!" --query "TargetGroups[0].TargetGroupArn" --output text') do set TARGET_GROUP_ARN=%%i
    
    echo Target Group created: !TARGET_GROUP_ARN!
    
    REM Create Listener
    echo Creating ALB Listener...
    aws elbv2 create-listener --load-balancer-arn "!ALB_ARN!" --protocol HTTP --port 80 --default-actions Type=forward,TargetGroupArn="!TARGET_GROUP_ARN!" --region "!AWS_REGION!" >nul
    
    echo ALB Listener created
    
    REM Get ALB DNS name
    for /f "tokens=*" %%i in ('aws elbv2 describe-load-balancers --load-balancer-arns "!ALB_ARN!" --region "!AWS_REGION!" --query "LoadBalancers[0].DNSName" --output text') do set ALB_DNS=%%i
    
    echo ALB DNS Name: !ALB_DNS!
)

REM Prepare task definition JSON
echo.
echo Preparing task definition...
set TASK_DEF_FILE=..\ecs\task-definition.json

REM Create temporary task definition with replaced placeholders
set TEMP_TASK_DEF=%TEMP%\task-def-%RANDOM%.json
powershell -Command "(Get-Content '%TASK_DEF_FILE%') -replace '{{ACCOUNT_ID}}', '!ACCOUNT_ID!' -replace '{{IMAGE_URI}}', '!IMAGE_URI!' -replace '{{AWS_REGION}}', '!AWS_REGION!' -replace '{{DB_HOST}}', '!DB_HOST!' -replace '{{DB_PORT}}', '!DB_PORT!' -replace '{{DB_NAME}}', '!DB_NAME!' -replace '{{DB_USERNAME}}', '!DB_USERNAME!' -replace '{{DB_PASSWORD}}', '!DB_PASSWORD!' | Set-Content '!TEMP_TASK_DEF!'"

REM Register task definition
echo Registering task definition...
for /f "tokens=*" %%i in ('aws ecs register-task-definition --cli-input-json file://!TEMP_TASK_DEF! --region "!AWS_REGION!" --query "taskDefinition.taskDefinitionArn" --output text') do set TASK_DEF_ARN=%%i

echo Task definition registered: !TASK_DEF_ARN!

REM Clean up temporary file
del /f /q "!TEMP_TASK_DEF!"

REM Prepare service definition JSON
echo.
echo Preparing service definition...
set SERVICE_DEF_FILE=..\ecs\service-definition.json

REM Create temporary service definition with replaced placeholders
set TEMP_SERVICE_DEF=%TEMP%\service-def-%RANDOM%.json

if /i "!NEED_LB!"=="y" (
    REM Include load balancer configuration
    powershell -Command "(Get-Content '%SERVICE_DEF_FILE%') -replace '{{CLUSTER_NAME}}', '!CLUSTER_NAME!' -replace '{{SUBNET_1}}', '!SUBNET_1!' -replace '{{SUBNET_2}}', '!SUBNET_2!' -replace '{{SECURITY_GROUP}}', '!SECURITY_GROUP!' -replace '{{TARGET_GROUP_ARN}}', '!TARGET_GROUP_ARN!' | Set-Content '!TEMP_SERVICE_DEF!'"
) else (
    REM Remove load balancer configuration
    powershell -Command "$json = Get-Content '%SERVICE_DEF_FILE%' | ConvertFrom-Json; $json.PSObject.Properties.Remove('loadBalancers'); $json.PSObject.Properties.Remove('healthCheckGracePeriodSeconds'); $json | ConvertTo-Json -Depth 10 | ForEach-Object { $_ -replace '{{CLUSTER_NAME}}', '!CLUSTER_NAME!' -replace '{{SUBNET_1}}', '!SUBNET_1!' -replace '{{SUBNET_2}}', '!SUBNET_2!' -replace '{{SECURITY_GROUP}}', '!SECURITY_GROUP!' } | Set-Content '!TEMP_SERVICE_DEF!'"
)

REM Check if service exists
echo Checking if service exists...
for /f "tokens=*" %%i in ('aws ecs describe-services --cluster "!CLUSTER_NAME!" --services "!SERVICE_NAME!" --region "!AWS_REGION!" --query "services[?status==`ACTIVE`].serviceName" --output text') do set SERVICE_EXISTS=%%i

if "!SERVICE_EXISTS!"=="" (
    REM Create new service
    echo Creating new ECS service: !SERVICE_NAME!
    aws ecs create-service --cli-input-json file://!TEMP_SERVICE_DEF! --region "!AWS_REGION!" >nul
    
    echo ECS service created successfully
) else (
    REM Update existing service
    echo Updating existing ECS service: !SERVICE_NAME!
    aws ecs update-service --cluster "!CLUSTER_NAME!" --service "!SERVICE_NAME!" --task-definition "!TASK_DEF_ARN!" --desired-count 2 --region "!AWS_REGION!" >nul
    
    echo ECS service updated successfully
)

REM Clean up temporary file
del /f /q "!TEMP_SERVICE_DEF!"

REM Wait for service to become stable
echo.
echo Waiting for service to become stable (this may take a few minutes)...
aws ecs wait services-stable --cluster "!CLUSTER_NAME!" --services "!SERVICE_NAME!" --region "!AWS_REGION!"

echo.
echo ==========================================
echo Deployment Completed Successfully
echo ==========================================
echo.

REM Display service information
echo Service Details:
aws ecs describe-services --cluster "!CLUSTER_NAME!" --services "!SERVICE_NAME!" --region "!AWS_REGION!" --query "services[0].[serviceName,status,runningCount,desiredCount]" --output table

echo.
echo CloudWatch Logs:
echo   Log Group: /ecs/!PROJECT_NAME!
echo   Region: !AWS_REGION!
echo.

if /i "!NEED_LB!"=="y" (
    echo Application URL:
    echo   http://!ALB_DNS!
    echo.
)

echo To view logs:
echo   aws logs tail /ecs/!PROJECT_NAME! --follow --region !AWS_REGION!
echo.
echo To check service status:
echo   aws ecs describe-services --cluster !CLUSTER_NAME! --services !SERVICE_NAME! --region !AWS_REGION!
echo.

endlocal
