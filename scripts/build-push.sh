#!/bin/bash
set -e
set -o pipefail

# ============================================================
# build-push.sh - Build and Push Docker Image for crm-testing
# ============================================================

PROJECT_NAME="crm-testing"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "=============================================="
echo "  CRM-Testing - Docker Build & Push Script"
echo "=============================================="
echo ""

# Sanitize image name: lowercase, replace non-alphanumeric with hyphens, trim hyphens
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

# Prompt for image tag
read -rp "Enter image tag [latest]: " IMAGE_TAG_INPUT
IMAGE_TAG=$(echo "${IMAGE_TAG_INPUT:-latest}" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9._-' '-' | sed 's/^-*//;s/-*$//')
if [ -z "$IMAGE_TAG" ]; then
  IMAGE_TAG="latest"
fi

echo ""
echo "Select container registry:"
echo "  1. AWS ECR (Elastic Container Registry)"
echo "  2. Docker Hub"
read -rp "Enter choice [1 or 2]: " REGISTRY_CHOICE

echo ""

# -------------------------------------------------------
# AWS ECR
# -------------------------------------------------------
if [ "$REGISTRY_CHOICE" = "1" ]; then
  read -rp "Enter AWS Region (e.g. us-east-1): " AWS_REGION
  if [ -z "$AWS_REGION" ]; then
    echo "ERROR: AWS Region is required." >&2
    exit 1
  fi

  read -rp "Enter AWS Account ID: " AWS_ACCOUNT_ID
  if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo "ERROR: AWS Account ID is required." >&2
    exit 1
  fi

  ECR_REPO="${IMAGE_NAME}"
  REGISTRY_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
  FULL_IMAGE_NAME="${REGISTRY_URL}/${ECR_REPO}:${IMAGE_TAG}"

  echo ""
  echo "Authenticating with AWS ECR..."
  aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$REGISTRY_URL"
  echo "ECR login successful."

  echo ""
  echo "Ensuring ECR repository exists..."
  aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$AWS_REGION" >/dev/null 2>&1 || \
    aws ecr create-repository --repository-name "$ECR_REPO" --region "$AWS_REGION"
  echo "ECR repository ready: $ECR_REPO"

# -------------------------------------------------------
# Docker Hub
# -------------------------------------------------------
elif [ "$REGISTRY_CHOICE" = "2" ]; then
  read -rp "Enter Docker Hub username: " DOCKER_USERNAME
  if [ -z "$DOCKER_USERNAME" ]; then
    echo "ERROR: Docker Hub username is required." >&2
    exit 1
  fi

  read -rsp "Enter Docker Hub password/token: " DOCKER_PASSWORD
  echo ""
  if [ -z "$DOCKER_PASSWORD" ]; then
    echo "ERROR: Docker Hub password/token is required." >&2
    exit 1
  fi

  REGISTRY_URL="docker.io"
  FULL_IMAGE_NAME="${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG}"

  echo ""
  echo "Authenticating with Docker Hub..."
  echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin
  echo "Docker Hub login successful."

else
  echo "ERROR: Invalid choice. Please enter 1 or 2." >&2
  exit 1
fi

echo ""
echo "----------------------------------------------"
echo "  Build Configuration"
echo "----------------------------------------------"
echo "  Image Name : $FULL_IMAGE_NAME"
echo "  Build Context: $PROJECT_ROOT"
echo "----------------------------------------------"
echo ""

echo "Building Docker image..."
docker build -f "$PROJECT_ROOT/Dockerfile" -t "$FULL_IMAGE_NAME" "$PROJECT_ROOT"
echo "Docker image built successfully."

echo ""
echo "Pushing Docker image to registry..."
docker push "$FULL_IMAGE_NAME"
echo "Docker image pushed successfully."

echo ""
echo "=============================================="
echo "  Build & Push Complete!"
echo "  Image: $FULL_IMAGE_NAME"
echo "=============================================="
