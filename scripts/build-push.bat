@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: build-push.bat - Build and Push Docker Image for crm-testing
:: ============================================================

set "PROJECT_NAME=crm-testing"
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

echo ==============================================
echo   CRM-Testing - Docker Build ^& Push Script
echo ==============================================
echo.

:: Sanitize image name using PowerShell
for /f "delims=" %%i in ('powershell -NoProfile -Command "$n = 'crm-testing'.ToLower() -replace '[^a-z0-9]','-'; $n = $n.Trim('-'); Write-Output $n"') do set "IMAGE_NAME=%%i"

:: Prompt for image tag
set /p "IMAGE_TAG_INPUT=Enter image tag [latest]: "
if "!IMAGE_TAG_INPUT!"=="" set "IMAGE_TAG_INPUT=latest"
for /f "delims=" %%i in ('powershell -NoProfile -Command "$t = '!IMAGE_TAG_INPUT!'.ToLower() -replace '[^a-z0-9._-]','-'; $t = $t.Trim('-'); if ($t -eq '') { $t = 'latest' }; Write-Output $t"') do set "IMAGE_TAG=%%i"
if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

echo.
echo Select container registry:
echo   1. AWS ECR (Elastic Container Registry)
echo   2. Docker Hub
set /p "REGISTRY_CHOICE=Enter choice [1 or 2]: "
echo.

:: -------------------------------------------------------
:: AWS ECR
:: -------------------------------------------------------
if "!REGISTRY_CHOICE!"=="1" (
    set /p "AWS_REGION=Enter AWS Region (e.g. us-east-1): "
    if "!AWS_REGION!"=="" (
        echo ERROR: AWS Region is required.
        exit /b 1
    )

    set /p "AWS_ACCOUNT_ID=Enter AWS Account ID: "
    if "!AWS_ACCOUNT_ID!"=="" (
        echo ERROR: AWS Account ID is required.
        exit /b 1
    )

    set "ECR_REPO=!IMAGE_NAME!"
    set "REGISTRY_URL=!AWS_ACCOUNT_ID!.dkr.ecr.!AWS_REGION!.amazonaws.com"
    set "FULL_IMAGE_NAME=!REGISTRY_URL!/!ECR_REPO!:!IMAGE_TAG!"

    echo.
    echo Authenticating with AWS ECR...
    aws ecr get-login-password --region !AWS_REGION! | docker login --username AWS --password-stdin !REGISTRY_URL!
    if !ERRORLEVEL! neq 0 (
        echo ERROR: ECR login failed.
        exit /b 1
    )
    echo ECR login successful.

    echo.
    echo Ensuring ECR repository exists...
    aws ecr describe-repositories --repository-names !ECR_REPO! --region !AWS_REGION! >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo Creating ECR repository: !ECR_REPO!
        aws ecr create-repository --repository-name !ECR_REPO! --region !AWS_REGION!
        if !ERRORLEVEL! neq 0 (
            echo ERROR: Failed to create ECR repository.
            exit /b 1
        )
    )
    echo ECR repository ready: !ECR_REPO!

:: -------------------------------------------------------
:: Docker Hub
:: -------------------------------------------------------
) else if "!REGISTRY_CHOICE!"=="2" (
    set /p "DOCKER_USERNAME=Enter Docker Hub username: "
    if "!DOCKER_USERNAME!"=="" (
        echo ERROR: Docker Hub username is required.
        exit /b 1
    )

    set /p "DOCKER_PASSWORD=Enter Docker Hub password/token: "
    if "!DOCKER_PASSWORD!"=="" (
        echo ERROR: Docker Hub password/token is required.
        exit /b 1
    )

    set "FULL_IMAGE_NAME=!DOCKER_USERNAME!/!IMAGE_NAME!:!IMAGE_TAG!"

    echo.
    echo Authenticating with Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo ERROR: Docker Hub login failed.
        exit /b 1
    )
    echo Docker Hub login successful.

) else (
    echo ERROR: Invalid choice. Please enter 1 or 2.
    exit /b 1
)

echo.
echo ----------------------------------------------
echo   Build Configuration
echo ----------------------------------------------
echo   Image Name : !FULL_IMAGE_NAME!
echo   Build Context: !PROJECT_ROOT!
echo ----------------------------------------------
echo.

echo Building Docker image...
docker build -f "!PROJECT_ROOT!\Dockerfile" -t "!FULL_IMAGE_NAME!" "!PROJECT_ROOT!"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Docker build failed.
    exit /b 1
)
echo Docker image built successfully.

echo.
echo Pushing Docker image to registry...
docker push "!FULL_IMAGE_NAME!"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Docker push failed.
    exit /b 1
)
echo Docker image pushed successfully.

echo.
echo ==============================================
echo   Build ^& Push Complete!
echo   Image: !FULL_IMAGE_NAME!
echo ==============================================

endlocal
