#!/bin/bash

set -e
set -o pipefail

echo "=========================================="
echo "GCP GKE Deployment Script"
echo "=========================================="
echo ""

# Prompt for GCP configuration
read -p "Enter GCP Project ID: " GCP_PROJECT
read -p "Enter GCP Zone (e.g., us-central1-a): " GCP_ZONE
read -p "Enter GKE Cluster Name: " CLUSTER_NAME

# Validate inputs
if [ -z "$GCP_PROJECT" ] || [ -z "$GCP_ZONE" ] || [ -z "$CLUSTER_NAME" ]; then
    echo "Error: All GCP configuration fields are required!"
    exit 1
fi

echo ""
echo "=== Authenticating with GCP ==="
gcloud auth login
gcloud config set project $GCP_PROJECT

echo ""
echo "=== Configuring kubectl for GKE ==="
gcloud container clusters get-credentials $CLUSTER_NAME --zone $GCP_ZONE --project $GCP_PROJECT

if [ $? -ne 0 ]; then
    echo "Error: Failed to configure kubectl for GKE cluster!"
    exit 1
fi

echo ""
echo "=== Verifying Cluster Connectivity ==="
kubectl cluster-info

if [ $? -ne 0 ]; then
    echo "Error: Cannot connect to Kubernetes cluster!"
    exit 1
fi

echo ""
read -p "Enter Docker Image URI (e.g., gcr.io/project/image:tag): " IMAGE_URI

if [ -z "$IMAGE_URI" ]; then
    echo "Error: Docker Image URI is required!"
    exit 1
fi

echo ""
echo "=== Environment Configuration ==="
echo "Configure application environment variables (press Enter to skip):"
echo ""

read -p "Enter SPRING_DATASOURCE_URL (e.g., jdbc:mysql://host:3306/crm): " SPRING_DATASOURCE_URL
SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:mysql://mysql-host:3306/crm?useSSL=false}

read -p "Enter SPRING_DATASOURCE_USERNAME: " SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-root}

read -sp "Enter SPRING_DATASOURCE_PASSWORD: " SPRING_DATASOURCE_PASSWORD
echo ""
SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-password}

echo ""
echo "=== Updating Kubernetes Manifests ==="

# Update deployment.yaml with image URI and environment variables
sed -i "s|{{IMAGE_URI}}|$IMAGE_URI|g" kubernetes/deployment.yaml
sed -i "s|{{SPRING_DATASOURCE_URL}}|$SPRING_DATASOURCE_URL|g" kubernetes/deployment.yaml
sed -i "s|{{SPRING_DATASOURCE_USERNAME}}|$SPRING_DATASOURCE_USERNAME|g" kubernetes/deployment.yaml
sed -i "s|{{SPRING_DATASOURCE_PASSWORD}}|$SPRING_DATASOURCE_PASSWORD|g" kubernetes/deployment.yaml

echo "Manifests updated successfully."

echo ""
echo "=== Applying Kubernetes Manifests ==="

# Apply namespace
echo "Creating namespace..."
kubectl apply -f kubernetes/namespace.yaml

# Apply deployment
echo "Deploying application..."
kubectl apply -f kubernetes/deployment.yaml

# Apply service
echo "Creating service..."
kubectl apply -f kubernetes/service.yaml

# Apply ingress
echo "Creating ingress..."
kubectl apply -f kubernetes/ingress.yaml

echo ""
echo "=== Waiting for Deployment Rollout ==="
kubectl rollout status deployment/crm-component-pruthwee -n crm-component-pruthwee --timeout=5m

if [ $? -ne 0 ]; then
    echo "Warning: Deployment rollout did not complete successfully!"
    echo "Check pod status with: kubectl get pods -n crm-component-pruthwee"
fi

echo ""
echo "=== Verifying Deployment ==="
kubectl get pods,svc,ingress -n crm-component-pruthwee

echo ""
echo "=========================================="
echo "Deployment Completed!"
echo "=========================================="
echo ""
echo "To check application logs:"
echo "  kubectl logs -f deployment/crm-component-pruthwee -n crm-component-pruthwee"
echo ""
echo "To check pod status:"
echo "  kubectl get pods -n crm-component-pruthwee"
echo ""
echo "To access the application:"
echo "  kubectl port-forward -n crm-component-pruthwee svc/crm-component-pruthwee-service 8080:80"
echo "  Then visit: http://localhost:8080"
echo ""
echo "To get ingress IP (may take a few minutes):"
echo "  kubectl get ingress -n crm-component-pruthwee"
echo ""
