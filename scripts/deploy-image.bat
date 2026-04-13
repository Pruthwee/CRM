@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo GCP GKE Deployment Script
echo ==========================================
echo.

REM Prompt for GCP configuration
set /p GCP_PROJECT="Enter GCP Project ID: "
set /p GCP_ZONE="Enter GCP Zone (e.g., us-central1-a): "
set /p CLUSTER_NAME="Enter GKE Cluster Name: "

REM Validate inputs
if "!GCP_PROJECT!"=="" (
    echo Error: GCP Project ID is required!
    exit /b 1
)
if "!GCP_ZONE!"=="" (
    echo Error: GCP Zone is required!
    exit /b 1
)
if "!CLUSTER_NAME!"=="" (
    echo Error: GKE Cluster Name is required!
    exit /b 1
)

echo.
echo === Authenticating with GCP ===
call gcloud auth login
if !ERRORLEVEL! neq 0 (
    echo Error: GCP authentication failed!
    exit /b 1
)

call gcloud config set project !GCP_PROJECT!
if !ERRORLEVEL! neq 0 (
    echo Error: Failed to set GCP project!
    exit /b 1
)

echo.
echo === Configuring kubectl for GKE ===
call gcloud container clusters get-credentials !CLUSTER_NAME! --zone !GCP_ZONE! --project !GCP_PROJECT!
if !ERRORLEVEL! neq 0 (
    echo Error: Failed to configure kubectl for GKE cluster!
    exit /b 1
)

echo.
echo === Verifying Cluster Connectivity ===
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo Error: Cannot connect to Kubernetes cluster!
    exit /b 1
)

echo.
set /p IMAGE_URI="Enter Docker Image URI (e.g., gcr.io/project/image:tag): "
if "!IMAGE_URI!"=="" (
    echo Error: Docker Image URI is required!
    exit /b 1
)

echo.
echo === Environment Configuration ===
echo Configure application environment variables (press Enter to skip):
echo.

set /p SPRING_DATASOURCE_URL="Enter SPRING_DATASOURCE_URL (e.g., jdbc:mysql://host:3306/crm): "
if "!SPRING_DATASOURCE_URL!"=="" set SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/crm?useSSL=false

set /p SPRING_DATASOURCE_USERNAME="Enter SPRING_DATASOURCE_USERNAME: "
if "!SPRING_DATASOURCE_USERNAME!"=="" set SPRING_DATASOURCE_USERNAME=root

set /p SPRING_DATASOURCE_PASSWORD="Enter SPRING_DATASOURCE_PASSWORD: "
if "!SPRING_DATASOURCE_PASSWORD!"=="" set SPRING_DATASOURCE_PASSWORD=password

echo.
echo === Updating Kubernetes Manifests ===

REM Update deployment.yaml with image URI and environment variables
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_URL}}', '!SPRING_DATASOURCE_URL!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_USERNAME}}', '!SPRING_DATASOURCE_USERNAME!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SPRING_DATASOURCE_PASSWORD}}', '!SPRING_DATASOURCE_PASSWORD!' | Set-Content kubernetes\deployment.yaml"

echo Manifests updated successfully.

echo.
echo === Applying Kubernetes Manifests ===

REM Apply namespace
echo Creating namespace...
kubectl apply -f kubernetes\namespace.yaml
if !ERRORLEVEL! neq 0 (
    echo Warning: Failed to create namespace
)

REM Apply deployment
echo Deploying application...
kubectl apply -f kubernetes\deployment.yaml
if !ERRORLEVEL! neq 0 (
    echo Error: Failed to deploy application!
    exit /b 1
)

REM Apply service
echo Creating service...
kubectl apply -f kubernetes\service.yaml
if !ERRORLEVEL! neq 0 (
    echo Error: Failed to create service!
    exit /b 1
)

REM Apply ingress
echo Creating ingress...
kubectl apply -f kubernetes\ingress.yaml
if !ERRORLEVEL! neq 0 (
    echo Warning: Failed to create ingress
)

echo.
echo === Waiting for Deployment Rollout ===
kubectl rollout status deployment/crm-component-pruthwee -n crm-component-pruthwee --timeout=5m
if !ERRORLEVEL! neq 0 (
    echo Warning: Deployment rollout did not complete successfully!
    echo Check pod status with: kubectl get pods -n crm-component-pruthwee
)

echo.
echo === Verifying Deployment ===
kubectl get pods,svc,ingress -n crm-component-pruthwee

echo.
echo ==========================================
echo Deployment Completed!
echo ==========================================
echo.
echo To check application logs:
echo   kubectl logs -f deployment/crm-component-pruthwee -n crm-component-pruthwee
echo.
echo To check pod status:
echo   kubectl get pods -n crm-component-pruthwee
echo.
echo To access the application:
echo   kubectl port-forward -n crm-component-pruthwee svc/crm-component-pruthwee-service 8080:80
echo   Then visit: http://localhost:8080
echo.
echo To get ingress IP (may take a few minutes):
echo   kubectl get ingress -n crm-component-pruthwee
echo.

endlocal
