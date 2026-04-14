#!/bin/bash

set -e
set -o pipefail

echo "=========================================="
echo "AWS EKS Deployment Script"
echo "=========================================="
echo ""

# Prompt for AWS region
read -p "Enter AWS region (e.g., us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
    echo "ERROR: AWS region is required"
    exit 1
fi

# Prompt for EKS cluster name
read -p "Enter EKS cluster name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
    echo "ERROR: EKS cluster name is required"
    exit 1
fi

# Prompt for Docker image URI
read -p "Enter Docker image URI (e.g., 123456789012.dkr.ecr.us-east-1.amazonaws.com/crm-container:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
    echo "ERROR: Docker image URI is required"
    exit 1
fi

echo ""
echo "=== Environment Variables Configuration ==="
echo "Configure application environment variables (press Enter to skip optional values)"
echo ""

# Database configuration
read -p "Enter SPRING_DATASOURCE_URL (default: jdbc:mysql://mysql-host:3306/crm?useSSL=false): " SPRING_DATASOURCE_URL
SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:mysql://mysql-host:3306/crm?useSSL=false}

read -p "Enter SPRING_DATASOURCE_USERNAME (default: root): " SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-root}

read -sp "Enter SPRING_DATASOURCE_PASSWORD (default: password): " SPRING_DATASOURCE_PASSWORD
echo ""
SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-password}

echo ""
echo "=== Configuration Summary ==="
echo "AWS Region: $AWS_REGION"
echo "EKS Cluster: $CLUSTER_NAME"
echo "Image URI: $IMAGE_URI"
echo "Database URL: $SPRING_DATASOURCE_URL"
echo "Database Username: $SPRING_DATASOURCE_USERNAME"
echo ""

# Configure kubectl for EKS
echo "=========================================="
echo "Configuring kubectl for EKS cluster..."
echo "=========================================="
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to configure kubectl for EKS cluster"
    exit 1
fi

echo "kubectl configured successfully"
echo ""

# Verify cluster connectivity
echo "Verifying cluster connectivity..."
kubectl cluster-info || {
    echo "ERROR: Cannot connect to Kubernetes cluster"
    exit 1
}
echo ""

# Update Kubernetes manifests with actual values
echo "=========================================="
echo "Updating Kubernetes manifests..."
echo "=========================================="

# Create temporary directory for updated manifests
TEMP_DIR=$(mktemp -d)
cp -r kubernetes/* "$TEMP_DIR/"

# Replace placeholders in deployment.yaml
sed -i "s|{{IMAGE_URI}}|$IMAGE_URI|g" "$TEMP_DIR/deployment.yaml"
sed -i "s|{{SPRING_DATASOURCE_URL}}|$SPRING_DATASOURCE_URL|g" "$TEMP_DIR/deployment.yaml"
sed -i "s|{{SPRING_DATASOURCE_USERNAME}}|$SPRING_DATASOURCE_USERNAME|g" "$TEMP_DIR/deployment.yaml"
sed -i "s|{{SPRING_DATASOURCE_PASSWORD}}|$SPRING_DATASOURCE_PASSWORD|g" "$TEMP_DIR/deployment.yaml"

echo "Manifests updated successfully"
echo ""

# Apply Kubernetes manifests
echo "=========================================="
echo "Deploying to AWS EKS..."
echo "=========================================="

echo "Creating namespace..."
kubectl apply -f "$TEMP_DIR/namespace.yaml"
echo ""

echo "Deploying application..."
kubectl apply -f "$TEMP_DIR/deployment.yaml"
echo ""

echo "Creating service..."
kubectl apply -f "$TEMP_DIR/service.yaml"
echo ""

echo "Creating ingress..."
kubectl apply -f "$TEMP_DIR/ingress.yaml"
echo ""

# Wait for deployment rollout
echo "=========================================="
echo "Waiting for deployment to complete..."
echo "=========================================="
kubectl rollout status deployment/crm-container -n crm-container --timeout=5m

if [ $? -ne 0 ]; then
    echo "ERROR: Deployment rollout failed"
    echo ""
    echo "Checking pod status..."
    kubectl get pods -n crm-container
    echo ""
    echo "Checking pod logs..."
    kubectl logs -n crm-container -l app=crm-container --tail=50
    exit 1
fi

echo ""
echo "Deployment completed successfully"
echo ""

# Verify deployment
echo "=========================================="
echo "Verifying deployment..."
echo "=========================================="
kubectl get pods,svc,ingress -n crm-container

echo ""
echo "=========================================="
echo "Deployment Information"
echo "=========================================="

# Get ingress URL
INGRESS_URL=$(kubectl get ingress crm-container-ingress -n crm-container -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "Pending...")

echo "Namespace: crm-container"
echo "Application: crm-container"
echo "Replicas: 2"
echo "Ingress URL: http://$INGRESS_URL"
echo ""
echo "Note: It may take a few minutes for the Load Balancer to become available."
echo ""

# Cleanup temporary directory
rm -rf "$TEMP_DIR"

echo "=========================================="
echo "Deployment Commands"
echo "=========================================="
echo "View pods:        kubectl get pods -n crm-container"
echo "View services:    kubectl get svc -n crm-container"
echo "View ingress:     kubectl get ingress -n crm-container"
echo "View logs:        kubectl logs -n crm-container -l app=crm-container"
echo "Describe pod:     kubectl describe pod -n crm-container -l app=crm-container"
echo "Scale deployment: kubectl scale deployment crm-container -n crm-container --replicas=3"
echo "Delete deployment: kubectl delete namespace crm-container"
echo ""

echo "=========================================="
echo "SUCCESS!"
echo "=========================================="
echo "Application deployed successfully to AWS EKS"
echo ""
