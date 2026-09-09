@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Deploy CRM to AWS EKS
echo ============================================
echo.

REM Prompt for AWS configuration
set /p AWS_REGION="Enter AWS Region (e.g., us-east-1): "
if "!AWS_REGION!"=="" (
    echo AWS Region is required. Exiting.
    exit /b 1
)

set /p CLUSTER_NAME="Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo EKS Cluster Name is required. Exiting.
    exit /b 1
)

set /p IMAGE_URI="Enter Docker Image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/crm:latest): "
if "!IMAGE_URI!"=="" (
    echo Docker Image URI is required. Exiting.
    exit /b 1
)

echo.
echo --- Application Environment Variables ---
set /p SPRING_DATASOURCE_URL="Enter SPRING_DATASOURCE_URL (e.g., jdbc:mysql://host:3306/crm?useSSL=false): "
set /p SPRING_DATASOURCE_USERNAME="Enter SPRING_DATASOURCE_USERNAME (default: root): "
if "!SPRING_DATASOURCE_USERNAME!"=="" set SPRING_DATASOURCE_USERNAME=root
set /p SPRING_DATASOURCE_PASSWORD="Enter SPRING_DATASOURCE_PASSWORD: "
set /p SPRING_JPA_HIBERNATE_DDL_AUTO="Enter SPRING_JPA_HIBERNATE_DDL_AUTO (default: update): "
if "!SPRING_JPA_HIBERNATE_DDL_AUTO!"=="" set SPRING_JPA_HIBERNATE_DDL_AUTO=update

echo.
echo Configuring kubectl for EKS cluster: !CLUSTER_NAME! in !AWS_REGION!...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo Failed to configure kubectl. Exiting.
    exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo Cluster connectivity check failed. Exiting.
    exit /b 1
)

echo.
echo Updating Kubernetes manifests with provided values...

powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_URL}}', '!SPRING_DATASOURCE_URL!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_USERNAME}}', '!SPRING_DATASOURCE_USERNAME!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_PASSWORD}}', '!SPRING_DATASOURCE_PASSWORD!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_JPA_HIBERNATE_DDL_AUTO}}', '!SPRING_JPA_HIBERNATE_DDL_AUTO!' | Set-Content kubernetes\deployment.yaml"

echo.
echo Applying Kubernetes manifests...

echo   [1/4] Applying namespace...
kubectl apply -f kubernetes\namespace.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply namespace. Exiting.
    exit /b 1
)

echo   [2/4] Applying deployment...
kubectl apply -f kubernetes\deployment.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply deployment. Exiting.
    exit /b 1
)

echo   [3/4] Applying service...
kubectl apply -f kubernetes\service.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply service. Exiting.
    exit /b 1
)

echo   [4/4] Applying ingress...
kubectl apply -f kubernetes\ingress.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply ingress. Exiting.
    exit /b 1
)

echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/crm -n crm --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed. Rolling back...
    kubectl rollout undo deployment/crm -n crm
    echo Rollback complete. Check pod logs: kubectl logs -l app=crm -n crm
    exit /b 1
)

echo.
echo Verifying deployed resources...
kubectl get pods,svc,ingress -n crm

echo.
echo ============================================
echo   Deployment Complete!
echo   Namespace: crm
echo   Image: !IMAGE_URI!
echo   Application URL: http://crm.example.com
echo   Health Check:    http://crm.example.com/appinfo/health
echo ============================================
echo.
echo Useful commands:
echo   kubectl get pods -n crm
echo   kubectl logs -l app=crm -n crm
echo   kubectl describe deployment crm -n crm
echo   kubectl rollout undo deployment/crm -n crm

endlocal
