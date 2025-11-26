@echo off
setlocal enabledelayedexpansion

REM Deploy to AWS EKS Script for CRM Application
echo =====================================
echo AWS EKS Deployment Script
echo =====================================
echo.

REM Configuration
set NAMESPACE=crm
set APP_NAME=crm-app

REM Prompt for EKS cluster details
echo === AWS EKS Configuration ===
set /p AWS_REGION="Enter AWS Region (e.g., us-east-1): "
set /p CLUSTER_NAME="Enter EKS Cluster Name: "
echo.

REM Prompt for Docker image URI
echo === Docker Image Configuration ===
set /p IMAGE_URI="Enter Docker Image URI: "
echo.

REM Prompt for environment variables
echo === Application Configuration ===
echo Configure database connection settings:
set /p DB_HOST="Enter Database Host [mysql-host]: "
if "!DB_HOST!"=="" set DB_HOST=mysql-host

set /p DB_PORT="Enter Database Port [3306]: "
if "!DB_PORT!"=="" set DB_PORT=3306

set /p DB_NAME="Enter Database Name [crm]: "
if "!DB_NAME!"=="" set DB_NAME=crm

set /p DDL_AUTO="Enter Hibernate DDL Auto Mode [validate]: "
if "!DDL_AUTO!"=="" set DDL_AUTO=validate
echo.

REM Configure kubectl for EKS
echo =====================================
echo Configuring kubectl for EKS
echo =====================================
echo.

aws eks update-kubeconfig --region "!AWS_REGION!" --name "!CLUSTER_NAME!"

if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl for EKS cluster
    exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info

if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to EKS cluster
    exit /b 1
)

echo.
echo =====================================
echo Updating Kubernetes Manifests
echo =====================================
echo.

REM Create temporary directory for manifests
set TMP_DIR=%TEMP%\k8s-deploy-%RANDOM%
mkdir "!TMP_DIR!"
xcopy /E /I /Q kubernetes\* "!TMP_DIR!\"

REM Update manifests with actual values using PowerShell
echo Updating image URI...
powershell -Command "(Get-Content '!TMP_DIR!\deployment.yaml') -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content '!TMP_DIR!\deployment.yaml'"

echo Updating database configuration...
powershell -Command "(Get-Content '!TMP_DIR!\deployment.yaml') -replace '{{DB_HOST}}', '!DB_HOST!' | Set-Content '!TMP_DIR!\deployment.yaml'"
powershell -Command "(Get-Content '!TMP_DIR!\deployment.yaml') -replace '{{DB_PORT}}', '!DB_PORT!' | Set-Content '!TMP_DIR!\deployment.yaml'"
powershell -Command "(Get-Content '!TMP_DIR!\deployment.yaml') -replace '{{DB_NAME}}', '!DB_NAME!' | Set-Content '!TMP_DIR!\deployment.yaml'"
powershell -Command "(Get-Content '!TMP_DIR!\deployment.yaml') -replace '{{DDL_AUTO}}', '!DDL_AUTO!' | Set-Content '!TMP_DIR!\deployment.yaml'"

echo.
echo =====================================
echo Deploying to EKS
echo =====================================
echo.

REM Apply manifests in order
echo Creating namespace...
kubectl apply -f "!TMP_DIR!\namespace.yaml"

echo Deploying application...
kubectl apply -f "!TMP_DIR!\deployment.yaml"

echo Creating service...
kubectl apply -f "!TMP_DIR!\service.yaml"

echo Creating ingress...
kubectl apply -f "!TMP_DIR!\ingress.yaml"

echo.
echo =====================================
echo Waiting for Deployment Rollout
echo =====================================
echo.

kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=5m

if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed
    echo.
    echo Checking pod status:
    kubectl get pods -n !NAMESPACE!
    echo.
    echo Recent events:
    kubectl get events -n !NAMESPACE! --sort-by=.lastTimestamp
    exit /b 1
)

echo.
echo =====================================
echo Deployment Status
echo =====================================
echo.

echo Pods:
kubectl get pods -n !NAMESPACE!
echo.

echo Services:
kubectl get svc -n !NAMESPACE!
echo.

echo Ingress:
kubectl get ingress -n !NAMESPACE!
echo.

REM Get ingress URL
for /f "delims=" %%i in ('kubectl get ingress !NAMESPACE!-ingress -n !NAMESPACE! -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" 2^>nul') do set INGRESS_URL=%%i
if "!INGRESS_URL!"=="" set INGRESS_URL=Pending...

echo =====================================
echo Deployment Completed Successfully
echo =====================================
echo.
echo Application URL: http://!INGRESS_URL!
echo Health Check: http://!INGRESS_URL!/appinfo/health
echo.
echo Useful commands:
echo   View logs: kubectl logs -f deployment/!APP_NAME! -n !NAMESPACE!
echo   View pods: kubectl get pods -n !NAMESPACE!
echo   Describe deployment: kubectl describe deployment !APP_NAME! -n !NAMESPACE!
echo   Rollback: kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
echo.

REM Cleanup
rmdir /S /Q "!TMP_DIR!"

echo Deployment script completed.

endlocal