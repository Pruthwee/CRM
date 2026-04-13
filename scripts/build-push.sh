#!/bin/bash

set -e

echo "=========================================="
echo "Docker Build and Push Script"
echo "=========================================="
echo ""

# Project configuration
PROJECT_NAME="CRM-component-pruthwee"

# Sanitize project name for Docker image naming
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

echo "Project: $PROJECT_NAME"
echo "Sanitized Image Name: $IMAGE_NAME"
echo ""

# Prompt for registry selection
echo "Select Docker Registry:"
echo "1. Google Artifact Registry"
echo "2. Docker Hub"
read -p "Enter choice (1 or 2): " REGISTRY_CHOICE

if [ "$REGISTRY_CHOICE" == "1" ]; then
    echo ""
    echo "=== Google Artifact Registry Configuration ==="
    
    # Prompt for GCP details
    read -p "Enter GCP Project ID: " GCP_PROJECT
    read -p "Enter GCP Region (e.g., us-central1): " GCP_REGION
    read -p "Enter Artifact Registry Repository Name: " AR_REPO
    
    # Authenticate with GCP
    echo ""
    echo "Authenticating with Google Cloud..."
    gcloud auth login
    gcloud config set project $GCP_PROJECT
    
    # Configure Docker for Artifact Registry
    echo "Configuring Docker for Artifact Registry..."
    gcloud auth configure-docker ${GCP_REGION}-docker.pkg.dev
    
    # Prompt for image tag
    read -p "Enter image tag (default: latest): " IMAGE_TAG
    IMAGE_TAG=${IMAGE_TAG:-latest}
    
    # Sanitize tag
    IMAGE_TAG=$(echo "$IMAGE_TAG" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9.-' '-' | sed 's/^-*//;s/-*$//')
    [ -z "$IMAGE_TAG" ] && IMAGE_TAG="latest"
    
    # Build full image name
    FULL_IMAGE_NAME="${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT}/${AR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}"
    
elif [ "$REGISTRY_CHOICE" == "2" ]; then
    echo ""
    echo "=== Docker Hub Configuration ==="
    
    # Prompt for Docker Hub credentials
    read -p "Enter Docker Hub username: " DOCKER_USERNAME
    read -sp "Enter Docker Hub password: " DOCKER_PASSWORD
    echo ""
    
    # Authenticate with Docker Hub
    echo "Authenticating with Docker Hub..."
    echo "$DOCKER_PASSWORD" | docker login --username $DOCKER_USERNAME --password-stdin
    
    # Prompt for image tag
    read -p "Enter image tag (default: latest): " IMAGE_TAG
    IMAGE_TAG=${IMAGE_TAG:-latest}
    
    # Sanitize tag
    IMAGE_TAG=$(echo "$IMAGE_TAG" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9.-' '-' | sed 's/^-*//;s/-*$//')
    [ -z "$IMAGE_TAG" ] && IMAGE_TAG="latest"
    
    # Build full image name
    FULL_IMAGE_NAME="${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG}"
    
else
    echo "Invalid choice. Exiting."
    exit 1
fi

echo ""
echo "=== Building Docker Image ==="
echo "Image: $FULL_IMAGE_NAME"
echo ""

# Build Docker image
docker build -t $FULL_IMAGE_NAME .

if [ $? -ne 0 ]; then
    echo "Docker build failed!"
    exit 1
fi

echo ""
echo "=== Pushing Docker Image ==="
docker push $FULL_IMAGE_NAME

if [ $? -ne 0 ]; then
    echo "Docker push failed!"
    exit 1
fi

echo ""
echo "=========================================="
echo "Build and Push Completed Successfully!"
echo "=========================================="
echo "Image: $FULL_IMAGE_NAME"
echo ""
