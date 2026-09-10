@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Deploy CRM to AWS EKS
echo ============================================
echo.

set /p AWS_REGION="Enter AWS Region (e.g. us-east-1): "
if "!AWS_REGION!"=="" (
    echo AWS Region is required. Exiting.
    exit /b 1
)

set /p CLUSTER_NAME="Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo EKS Cluster Name is required. Exiting.
    exit /b 1
)

set /p IMAGE_URI="Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/crm:latest): "
if "!IMAGE_URI!"=="" (
    echo Docker image URI is required. Exiting.
    exit /b 1
)

echo.
echo --- Application Environment Variables ---
echo (Press Enter to skip any variable)

set /p SPRING_DATASOURCE_URL="Enter SPRING_DATASOURCE_URL (e.g. jdbc:mysql://host:3306/crm): "
if "!SPRING_DATASOURCE_URL!"=="" (
    set SPRING_DATASOURCE_URL=jdbc:mysql://db-host:3306/crm?useSSL=false
)

set /p SPRING_DATASOURCE_USERNAME="Enter SPRING_DATASOURCE_USERNAME (default: root): "
if "!SPRING_DATASOURCE_USERNAME!"=="" (
    set SPRING_DATASOURCE_USERNAME=root
)

set /p SPRING_DATASOURCE_PASSWORD="Enter SPRING_DATASOURCE_PASSWORD: "
if "!SPRING_DATASOURCE_PASSWORD!"=="" (
    set SPRING_DATASOURCE_PASSWORD=password
)

echo.
echo --- Configuring kubectl for EKS ---
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
echo --- Updating Kubernetes manifests ---
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_URL}}', '!SPRING_DATASOURCE_URL!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_USERNAME}}', '!SPRING_DATASOURCE_USERNAME!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_PASSWORD}}', '!SPRING_DATASOURCE_PASSWORD!' | Set-Content kubernetes\deployment.yaml"

echo.
echo --- Applying Kubernetes manifests ---
echo Applying namespace...
kubectl apply -f kubernetes\namespace.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply namespace. Exiting.
    exit /b 1
)

echo Applying deployment...
kubectl apply -f kubernetes\deployment.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply deployment. Exiting.
    exit /b 1
)

echo Applying service...
kubectl apply -f kubernetes\service.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply service. Exiting.
    exit /b 1
)

echo Applying ingress...
kubectl apply -f kubernetes\ingress.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply ingress. Exiting.
    exit /b 1
)

echo.
echo --- Waiting for deployment rollout ---
kubectl rollout status deployment/crm -n crm
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed. Rolling back...
    kubectl rollout undo deployment/crm -n crm
    exit /b 1
)

echo.
echo --- Verifying resources ---
kubectl get pods,svc,ingress -n crm

echo.
echo ============================================
echo   Deployment completed successfully!
echo ============================================
echo.
echo Rollback command (if needed):
echo   kubectl rollout undo deployment/crm -n crm

endlocal
