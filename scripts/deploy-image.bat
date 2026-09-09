@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: deploy-image.bat - Deploy crm-testing to AWS EKS (Windows)
:: ============================================================

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "K8S_DIR=%PROJECT_ROOT%\kubernetes"
set "APP_NAME=crm-testing"
set "NAMESPACE=crm-testing"

echo ==============================================
echo   CRM-Testing - AWS EKS Deployment Script
echo ==============================================
echo.

:: -------------------------------------------------------
:: Collect deployment inputs
:: -------------------------------------------------------
set /p "AWS_REGION=Enter AWS Region (e.g. us-east-1): "
if "!AWS_REGION!"=="" (
    echo ERROR: AWS Region is required.
    exit /b 1
)

set /p "CLUSTER_NAME=Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS Cluster Name is required.
    exit /b 1
)

set /p "IMAGE_URI=Enter full Docker Image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/crm-testing:latest): "
if "!IMAGE_URI!"=="" (
    echo ERROR: Docker Image URI is required.
    exit /b 1
)

echo.
echo --- Optional: Application Environment Variables ---
echo Press Enter to skip any variable.
echo.

set /p "DB_URL_VAL=Enter DB_URL (e.g. jdbc:mysql://host:3306/crmdb) [skip]: "
set /p "DB_USERNAME_VAL=Enter DB_USERNAME [skip]: "
set /p "DB_PASSWORD_VAL=Enter DB_PASSWORD [skip]: "

:: -------------------------------------------------------
:: Configure kubectl for EKS
:: -------------------------------------------------------
echo.
echo Configuring kubectl for EKS cluster: !CLUSTER_NAME! in !AWS_REGION!...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl for EKS.
    exit /b 1
)
echo kubectl configured successfully.

echo.
echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to EKS cluster.
    exit /b 1
)

:: -------------------------------------------------------
:: Update Kubernetes manifests with actual values
:: -------------------------------------------------------
echo.
echo Updating Kubernetes manifests with deployment values...

:: Copy deployment.yaml to a working copy
copy "!K8S_DIR!\deployment.yaml" "!K8S_DIR!\deployment.yaml.deploy" >nul

:: Replace IMAGE_URI placeholder
powershell -NoProfile -Command "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"

:: Replace DB_URL placeholder
if "!DB_URL_VAL!"=="" set "DB_URL_VAL=jdbc:h2:mem:crmdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
powershell -NoProfile -Command "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{DB_URL}}', '!DB_URL_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"

:: Replace DB_USERNAME placeholder
if "!DB_USERNAME_VAL!"=="" set "DB_USERNAME_VAL=sa"
powershell -NoProfile -Command "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{DB_USERNAME}}', '!DB_USERNAME_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"

:: Replace DB_PASSWORD placeholder
powershell -NoProfile -Command "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{DB_PASSWORD}}', '!DB_PASSWORD_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"

echo Manifests updated.

:: -------------------------------------------------------
:: Apply Kubernetes manifests in order
:: -------------------------------------------------------
echo.
echo Applying Kubernetes manifests...

echo   [1/4] Applying namespace...
kubectl apply -f "!K8S_DIR!\namespace.yaml"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply namespace.
    exit /b 1
)

echo   [2/4] Applying deployment...
kubectl apply -f "!K8S_DIR!\deployment.yaml.deploy"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply deployment.
    del "!K8S_DIR!\deployment.yaml.deploy" >nul 2>&1
    exit /b 1
)

echo   [3/4] Applying service...
kubectl apply -f "!K8S_DIR!\service.yaml"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply service.
    del "!K8S_DIR!\deployment.yaml.deploy" >nul 2>&1
    exit /b 1
)

echo   [4/4] Applying ingress...
kubectl apply -f "!K8S_DIR!\ingress.yaml"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply ingress.
    del "!K8S_DIR!\deployment.yaml.deploy" >nul 2>&1
    exit /b 1
)

:: Clean up temporary file
del "!K8S_DIR!\deployment.yaml.deploy" >nul 2>&1

:: -------------------------------------------------------
:: Wait for rollout
:: -------------------------------------------------------
echo.
echo Waiting for deployment rollout to complete...
kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed or timed out.
    echo To rollback, run: kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
    exit /b 1
)
echo Deployment rollout complete.

:: -------------------------------------------------------
:: Verify resources
:: -------------------------------------------------------
echo.
echo Verifying deployed resources in namespace: !NAMESPACE!
kubectl get pods,svc,ingress -n !NAMESPACE!

:: -------------------------------------------------------
:: Display application URL
:: -------------------------------------------------------
echo.
echo Fetching application ingress URL...
for /f "delims=" %%h in ('kubectl get ingress !APP_NAME!-ingress -n !NAMESPACE! -o jsonpath^="{.status.loadBalancer.ingress[0].hostname}" 2^>nul') do set "INGRESS_HOST=%%h"

if not "!INGRESS_HOST!"=="" (
    echo.
    echo ==============================================
    echo   Application is accessible at:
    echo   http://!INGRESS_HOST!
    echo   Health Check: http://!INGRESS_HOST!/appinfo/health
    echo ==============================================
) else (
    echo INFO: Ingress hostname not yet assigned. Run:
    echo   kubectl get ingress !APP_NAME!-ingress -n !NAMESPACE!
)

echo.
echo --- Rollback Instructions ---
echo To rollback to the previous deployment, run:
echo   kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
echo.
echo ==============================================
echo   Deployment Complete!
echo ==============================================

endlocal
