#!/bin/bash

# Cloud Deployment Script for CRM Application
# Supports GKE, Cloud Run, and App Engine deployments

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ID="${GCP_PROJECT_ID:-}"
REGION="${GCP_REGION:-us-central1}"
IMAGE_NAME="crm-app"
IMAGE_TAG="${IMAGE_TAG:-latest}"

# Functions
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    print_info "Checking prerequisites..."
    
    if [ -z "$PROJECT_ID" ]; then
        print_error "GCP_PROJECT_ID environment variable is not set"
        exit 1
    fi
    
    if ! command -v gcloud &> /dev/null; then
        print_error "gcloud CLI is not installed"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    
    print_info "Prerequisites check passed"
}

build_image() {
    print_info "Building Docker image..."
    docker build -t gcr.io/${PROJECT_ID}/${IMAGE_NAME}:${IMAGE_TAG} .
    print_info "Docker image built successfully"
}

push_image() {
    print_info "Pushing image to Google Container Registry..."
    docker push gcr.io/${PROJECT_ID}/${IMAGE_NAME}:${IMAGE_TAG}
    print_info "Image pushed successfully"
}

deploy_to_gke() {
    print_info "Deploying to Google Kubernetes Engine..."
    
    # Update deployment manifest with project ID
    sed -i.bak "s/PROJECT_ID/${PROJECT_ID}/g" k8s/deployment.yaml
    
    # Apply Kubernetes manifests
    kubectl apply -f k8s/deployment.yaml
    
    # Restore original manifest
    mv k8s/deployment.yaml.bak k8s/deployment.yaml
    
    print_info "Deployment to GKE completed"
    print_info "Check status with: kubectl get pods -l app=crm-app"
}

deploy_to_cloud_run() {
    print_info "Deploying to Cloud Run..."
    
    gcloud run deploy crm-app \
        --image gcr.io/${PROJECT_ID}/${IMAGE_NAME}:${IMAGE_TAG} \
        --platform managed \
        --region ${REGION} \
        --allow-unauthenticated \
        --set-env-vars "GCS_BUCKET_NAME=crm-storage-bucket,GCP_PROJECT_ID=${PROJECT_ID}" \
        --memory 1Gi \
        --cpu 1 \
        --min-instances 1 \
        --max-instances 10 \
        --timeout 300
    
    print_info "Deployment to Cloud Run completed"
    print_info "Get service URL with: gcloud run services describe crm-app --region ${REGION} --format 'value(status.url)'"
}

deploy_to_app_engine() {
    print_info "Deploying to App Engine..."
    
    # Update app.yaml with project ID
    sed -i.bak "s/your-project-id/${PROJECT_ID}/g" app.yaml
    
    gcloud app deploy app.yaml --project ${PROJECT_ID}
    
    # Restore original app.yaml
    mv app.yaml.bak app.yaml
    
    print_info "Deployment to App Engine completed"
    print_info "Access application at: https://${PROJECT_ID}.appspot.com"
}

# Main script
main() {
    echo "========================================="
    echo "CRM Application Cloud Deployment"
    echo "========================================="
    echo ""
    
    if [ $# -eq 0 ]; then
        print_error "Usage: $0 [gke|cloudrun|appengine]"
        exit 1
    fi
    
    DEPLOYMENT_TARGET=$1
    
    check_prerequisites
    
    # Set GCP project
    gcloud config set project ${PROJECT_ID}
    
    # Build and push image
    build_image
    push_image
    
    # Deploy to target platform
    case $DEPLOYMENT_TARGET in
        gke)
            deploy_to_gke
            ;;
        cloudrun)
            deploy_to_cloud_run
            ;;
        appengine)
            deploy_to_app_engine
            ;;
        *)
            print_error "Invalid deployment target: $DEPLOYMENT_TARGET"
            print_error "Valid options: gke, cloudrun, appengine"
            exit 1
            ;;
    esac
    
    echo ""
    print_info "Deployment completed successfully!"
}

main "$@"
