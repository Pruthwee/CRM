@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo AWS EKS Deployment Script
echo ==========================================
echo.

REM Prompt for AWS region
set /p AWS_REGION="Enter AWS region (e.g., us-east-1): "
if "!AWS_REGION!"=="" (
    echo ERROR: AWS region is required
    exit /b 1
)

REM Prompt for EKS cluster name
set /p CLUSTER_NAME="Enter EKS cluster name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS cluster name is required
    exit /b 1
)

REM Prompt for Docker image URI
set /p IMAGE_URI="Enter Docker image URI (e.g., 123456789012.dkr.ecr.us-east-1.amazonaws.com/crm-container:latest): "
if "!IMAGE_URI!"=="" (
    echo ERROR: Docker image URI is required
    exit /b 1
)

echo.
echo === Environment Variables Configuration ===
echo Configure application environment variables (press Enter to skip optional values)
echo.

REM Database configuration
set /p SPRING_DATASOURCE_URL="Enter SPRING_DATASOURCE_URL (default: jdbc:mysql://mysql-host:3306/crm?useSSL=false): "
if "!SPRING_DATASOURCE_URL!"=="" set SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/crm?useSSL=false

set /p SPRING_DATASOURCE_USERNAME="Enter SPRING_DATASOURCE_USERNAME (default: root): "
if "!SPRING_DATASOURCE_USERNAME!"=="" set SPRING_DATASOURCE_USERNAME=root

set /p SPRING_DATASOURCE_PASSWORD="Enter SPRING_DATASOURCE_PASSWORD (default: password): "
if "!SPRING_DATASOURCE_PASSWORD!"=="" set SPRING_DATASOURCE_PASSWORD=password

echo.
echo === Configuration Summary ===
echo AWS Region: !AWS_REGION!
echo EKS Cluster: !CLUSTER_NAME!
echo Image URI: !IMAGE_URI!
echo Database URL: !SPRING_DATASOURCE_URL!
echo Database Username: !SPRING_DATASOURCE_USERNAME!
echo.

REM Configure kubectl for EKS
echo ==========================================
echo Configuring kubectl for EKS cluster...
echo ==========================================
aws eks update-kubeconfig --region "!AWS_REGION!" --name "!CLUSTER_NAME!"

if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl for EKS cluster
    exit /b 1
)

echo kubectl configured successfully
echo.

REM Verify cluster connectivity
echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to Kubernetes cluster
    exit /b 1
)
echo.

REM Update Kubernetes manifests with actual values
echo ==========================================
echo Updating Kubernetes manifests...
echo ==========================================

REM Create temporary directory for updated manifests
set TEMP_DIR=%TEMP%\k8s-deploy-%RANDOM%
mkdir "!TEMP_DIR!"
xcopy /E /I /Q kubernetes "!TEMP_DIR!" >nul

REM Replace placeholders in deployment.yaml using PowerShell
powershell -Command "(Get-Content '!TEMP_DIR!\deployment.yaml') -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content '!TEMP_DIR!\deployment.yaml'"
powershell -Command "(Get-Content '!TEMP_DIR!\deployment.yaml') -replace '{{SPRING_DATASOURCE_URL}}', '!SPRING_DATASOURCE_URL!' | Set-Content '!TEMP_DIR!\deployment.yaml'"
powershell -Command "(Get-Content '!TEMP_DIR!\deployment.yaml') -replace '{{SPRING_DATASOURCE_USERNAME}}', '!SPRING_DATASOURCE_USERNAME!' | Set-Content '!TEMP_DIR!\deployment.yaml'"
powershell -Command "(Get-Content '!TEMP_DIR!\deployment.yaml') -replace '{{SPRING_DATASOURCE_PASSWORD}}', '!SPRING_DATASOURCE_PASSWORD!' | Set-Content '!TEMP_DIR!\deployment.yaml'"

echo Manifests updated successfully
echo.

REM Apply Kubernetes manifests
echo ==========================================
echo Deploying to AWS EKS...
echo ==========================================

echo Creating namespace...
kubectl apply -f "!TEMP_DIR!\namespace.yaml"
echo.

echo Deploying application...
kubectl apply -f "!TEMP_DIR!\deployment.yaml"
echo.

echo Creating service...
kubectl apply -f "!TEMP_DIR!\service.yaml"
echo.

echo Creating ingress...
kubectl apply -f "!TEMP_DIR!\ingress.yaml"
echo.

REM Wait for deployment rollout
echo ==========================================
echo Waiting for deployment to complete...
echo ==========================================
kubectl rollout status deployment/crm-container -n crm-container --timeout=5m

if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed
    echo.
    echo Checking pod status...
    kubectl get pods -n crm-container
    echo.
    echo Checking pod logs...
    kubectl logs -n crm-container -l app=crm-container --tail=50
    exit /b 1
)

echo.
echo Deployment completed successfully
echo.

REM Verify deployment
echo ==========================================
echo Verifying deployment...
echo ==========================================
kubectl get pods,svc,ingress -n crm-container

echo.
echo ==========================================
echo Deployment Information
echo ==========================================

REM Get ingress URL
for /f "delims=" %%i in ('kubectl get ingress crm-container-ingress -n crm-container -o jsonpath^="{.status.loadBalancer.ingress[0].hostname}" 2^>nul') do set INGRESS_URL=%%i
if "!INGRESS_URL!"=="" set INGRESS_URL=Pending...

echo Namespace: crm-container
echo Application: crm-container
echo Replicas: 2
echo Ingress URL: http://!INGRESS_URL!
echo.
echo Note: It may take a few minutes for the Load Balancer to become available.
echo.

REM Cleanup temporary directory
rmdir /S /Q "!TEMP_DIR!"

echo ==========================================
echo Deployment Commands
echo ==========================================
echo View pods:        kubectl get pods -n crm-container
echo View services:    kubectl get svc -n crm-container
echo View ingress:     kubectl get ingress -n crm-container
echo View logs:        kubectl logs -n crm-container -l app=crm-container
echo Describe pod:     kubectl describe pod -n crm-container -l app=crm-container
echo Scale deployment: kubectl scale deployment crm-container -n crm-container --replicas=3
echo Delete deployment: kubectl delete namespace crm-container
echo.

echo ==========================================
echo SUCCESS!
echo ==========================================
echo Application deployed successfully to AWS EKS
echo.

endlocal
