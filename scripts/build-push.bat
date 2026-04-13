@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo Docker Build and Push Script
echo ==========================================
echo.

REM Project configuration
set PROJECT_NAME=CRM-component-pruthwee

REM Sanitize project name for Docker image naming
set IMAGE_NAME=%PROJECT_NAME%
set IMAGE_NAME=!IMAGE_NAME: =-!
set IMAGE_NAME=!IMAGE_NAME:_=-!
for %%i in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do set IMAGE_NAME=!IMAGE_NAME:%%i=%%i!
set IMAGE_NAME=%IMAGE_NAME: =%
set IMAGE_NAME=%IMAGE_NAME:~0,128%

echo Project: %PROJECT_NAME%
echo Sanitized Image Name: !IMAGE_NAME!
echo.

REM Prompt for registry selection
echo Select Docker Registry
echo 1. Google Artifact Registry
echo 2. Docker Hub
set /p REGISTRY_CHOICE="Enter choice (1 or 2): "

if "!REGISTRY_CHOICE!"=="1" (
    echo.
    echo === Google Artifact Registry Configuration ===
    
    REM Prompt for GCP details
    set /p GCP_PROJECT="Enter GCP Project ID: "
    set /p GCP_REGION="Enter GCP Region (e.g., us-central1): "
    set /p AR_REPO="Enter Artifact Registry Repository Name: "
    
    REM Authenticate with GCP
    echo.
    echo Authenticating with Google Cloud...
    call gcloud auth login
    if !ERRORLEVEL! neq 0 (
        echo GCP authentication failed!
        exit /b 1
    )
    
    call gcloud config set project !GCP_PROJECT!
    if !ERRORLEVEL! neq 0 (
        echo Failed to set GCP project!
        exit /b 1
    )
    
    REM Configure Docker for Artifact Registry
    echo Configuring Docker for Artifact Registry...
    call gcloud auth configure-docker !GCP_REGION!-docker.pkg.dev
    if !ERRORLEVEL! neq 0 (
        echo Artifact Registry authentication failed!
        exit /b 1
    )
    
    REM Prompt for image tag
    set /p IMAGE_TAG="Enter image tag (default: latest): "
    if "!IMAGE_TAG!"=="" set IMAGE_TAG=latest
    
    REM Build full image name
    set FULL_IMAGE_NAME=!GCP_REGION!-docker.pkg.dev/!GCP_PROJECT!/!AR_REPO!/!IMAGE_NAME!:!IMAGE_TAG!
    
) else if "!REGISTRY_CHOICE!"=="2" (
    echo.
    echo === Docker Hub Configuration ===
    
    REM Prompt for Docker Hub credentials
    set /p DOCKER_USERNAME="Enter Docker Hub username: "
    set /p DOCKER_PASSWORD="Enter Docker Hub password: "
    
    REM Authenticate with Docker Hub
    echo Authenticating with Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo Docker Hub authentication failed!
        exit /b 1
    )
    
    REM Prompt for image tag
    set /p IMAGE_TAG="Enter image tag (default: latest): "
    if "!IMAGE_TAG!"=="" set IMAGE_TAG=latest
    
    REM Build full image name
    set FULL_IMAGE_NAME=!DOCKER_USERNAME!/!IMAGE_NAME!:!IMAGE_TAG!
    
) else (
    echo Invalid choice. Exiting.
    exit /b 1
)

echo.
echo === Building Docker Image ===
echo Image: !FULL_IMAGE_NAME!
echo.

REM Build Docker image
docker build -t !FULL_IMAGE_NAME! .
if !ERRORLEVEL! neq 0 (
    echo Docker build failed!
    exit /b 1
)

echo.
echo === Pushing Docker Image ===
docker push !FULL_IMAGE_NAME!
if !ERRORLEVEL! neq 0 (
    echo Docker push failed!
    exit /b 1
)

echo.
echo ==========================================
echo Build and Push Completed Successfully!
echo ==========================================
echo Image: !FULL_IMAGE_NAME!
echo.

endlocal
