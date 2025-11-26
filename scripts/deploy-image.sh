#!/bin/bash
set -e
set -o pipefail

# Deploy to AWS EKS Script for CRM Application
echo "====================================="
echo "AWS EKS Deployment Script"
echo "====================================="
echo ""

# Configuration
NAMESPACE="crm"
APP_NAME="crm-app"

# Prompt for EKS cluster details
echo "=== AWS EKS Configuration ==="
read -p "Enter AWS Region (e.g., us-east-1): " AWS_REGION
read -p "Enter EKS Cluster Name: " CLUSTER_NAME
echo ""

# Prompt for Docker image URI
echo "=== Docker Image Configuration ==="
read -p "Enter Docker Image URI (e.g., 123456789012.dkr.ecr.us-east-1.amazonaws.com/crm:latest): " IMAGE_URI
echo ""

# Prompt for environment variables
echo "=== Application Configuration ==="
echo "Configure database connection settings:"
read -p "Enter Database Host [mysql-host]: " DB_HOST
DB_HOST=${DB_HOST:-mysql-host}

read -p "Enter Database Port [3306]: " DB_PORT
DB_PORT=${DB_PORT:-3306}

read -p "Enter Database Name [crm]: " DB_NAME
DB_NAME=${DB_NAME:-crm}

read -p "Enter Hibernate DDL Auto Mode [validate]: " DDL_AUTO
DDL_AUTO=${DDL_AUTO:-validate}
echo ""

# Configure kubectl for EKS
echo "====================================="
echo "Configuring kubectl for EKS"
echo "====================================="
echo ""

aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to configure kubectl for EKS cluster"
    exit 1
fi

echo "Verifying cluster connectivity..."
kubectl cluster-info || {
    echo "ERROR: Cannot connect to EKS cluster"
    exit 1
}

echo ""
echo "====================================="
echo "Updating Kubernetes Manifests"
echo "====================================="
echo ""

# Create temporary directory for manifests
TMP_DIR=$(mktemp -d)
cp -r kubernetes/* "$TMP_DIR/"

# Update manifests with actual values
echo "Updating image URI..."
sed -i "s|{{IMAGE_URI}}|$IMAGE_URI|g" "$TMP_DIR/deployment.yaml"

echo "Updating database configuration..."
sed -i "s|{{DB_HOST}}|$DB_HOST|g" "$TMP_DIR/deployment.yaml"
sed -i "s|{{DB_PORT}}|$DB_PORT|g" "$TMP_DIR/deployment.yaml"
sed -i "s|{{DB_NAME}}|$DB_NAME|g" "$TMP_DIR/deployment.yaml"
sed -i "s|{{DDL_AUTO}}|$DDL_AUTO|g" "$TMP_DIR/deployment.yaml"

echo ""
echo "====================================="
echo "Deploying to EKS"
echo "====================================="
echo ""

# Apply manifests in order
echo "Creating namespace..."
kubectl apply -f "$TMP_DIR/namespace.yaml"

echo "Deploying application..."
kubectl apply -f "$TMP_DIR/deployment.yaml"

echo "Creating service..."
kubectl apply -f "$TMP_DIR/service.yaml"

echo "Creating ingress..."
kubectl apply -f "$TMP_DIR/ingress.yaml"

echo ""
echo "====================================="
echo "Waiting for Deployment Rollout"
echo "====================================="
echo ""

kubectl rollout status deployment/$APP_NAME -n $NAMESPACE --timeout=5m

if [ $? -ne 0 ]; then
    echo "ERROR: Deployment rollout failed"
    echo ""
    echo "Checking pod status:"
    kubectl get pods -n $NAMESPACE
    echo ""
    echo "Recent events:"
    kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -20
    exit 1
fi

echo ""
echo "====================================="
echo "Deployment Status"
echo "====================================="
echo ""

echo "Pods:"
kubectl get pods -n $NAMESPACE
echo ""

echo "Services:"
kubectl get svc -n $NAMESPACE
echo ""

echo "Ingress:"
kubectl get ingress -n $NAMESPACE
echo ""

# Get ingress URL
INGRESS_URL=$(kubectl get ingress $NAMESPACE-ingress -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "Pending...")

echo "====================================="
echo "Deployment Completed Successfully"
echo "====================================="
echo ""
echo "Application URL: http://$INGRESS_URL"
echo "Health Check: http://$INGRESS_URL/appinfo/health"
echo ""
echo "Useful commands:"
echo "  View logs: kubectl logs -f deployment/$APP_NAME -n $NAMESPACE"
echo "  View pods: kubectl get pods -n $NAMESPACE"
echo "  Describe deployment: kubectl describe deployment $APP_NAME -n $NAMESPACE"
echo "  Rollback: kubectl rollout undo deployment/$APP_NAME -n $NAMESPACE"
echo ""

# Cleanup
rm -rf "$TMP_DIR"

echo "Deployment script completed."