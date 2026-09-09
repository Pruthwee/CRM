#!/bin/bash
set -e
set -o pipefail

# ============================================================
# deploy-image.sh - Deploy crm-testing to AWS EKS
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
K8S_DIR="$PROJECT_ROOT/kubernetes"
APP_NAME="crm-testing"
NAMESPACE="crm-testing"

echo "=============================================="
echo "  CRM-Testing - AWS EKS Deployment Script"
echo "=============================================="
echo ""

# -------------------------------------------------------
# Collect deployment inputs
# -------------------------------------------------------
read -rp "Enter AWS Region (e.g. us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "ERROR: AWS Region is required." >&2
  exit 1
fi

read -rp "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "ERROR: EKS Cluster Name is required." >&2
  exit 1
fi

read -rp "Enter full Docker Image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/crm-testing:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "ERROR: Docker Image URI is required." >&2
  exit 1
fi

echo ""
echo "--- Optional: Application Environment Variables ---"
echo "Press Enter to skip any variable (placeholder will remain in manifest)."
echo ""

read -rp "Enter DB_URL (e.g. jdbc:mysql://host:3306/crmdb) [skip]: " DB_URL_VAL
read -rp "Enter DB_USERNAME [skip]: " DB_USERNAME_VAL
read -rsp "Enter DB_PASSWORD [skip]: " DB_PASSWORD_VAL
echo ""

# -------------------------------------------------------
# Configure kubectl for EKS
# -------------------------------------------------------
echo ""
echo "Configuring kubectl for EKS cluster: $CLUSTER_NAME in $AWS_REGION..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"
echo "kubectl configured successfully."

echo ""
echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "ERROR: Cannot connect to EKS cluster." >&2; exit 1; }

# -------------------------------------------------------
# Update Kubernetes manifests with actual values
# -------------------------------------------------------
echo ""
echo "Updating Kubernetes manifests with deployment values..."

# Work on copies to avoid modifying originals
cp "$K8S_DIR/deployment.yaml" "$K8S_DIR/deployment.yaml.deploy"

# Replace IMAGE_URI placeholder
sed -i 's|{{IMAGE_URI}}|'"$IMAGE_URI"'|g' "$K8S_DIR/deployment.yaml.deploy"

# Replace DB_URL placeholder
if [ -n "$DB_URL_VAL" ]; then
  sed -i 's|{{DB_URL}}|'"$DB_URL_VAL"'|g' "$K8S_DIR/deployment.yaml.deploy"
else
  sed -i 's|{{DB_URL}}|jdbc:h2:mem:crmdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE|g' "$K8S_DIR/deployment.yaml.deploy"
fi

# Replace DB_USERNAME placeholder
if [ -n "$DB_USERNAME_VAL" ]; then
  sed -i 's|{{DB_USERNAME}}|'"$DB_USERNAME_VAL"'|g' "$K8S_DIR/deployment.yaml.deploy"
else
  sed -i 's|{{DB_USERNAME}}|sa|g' "$K8S_DIR/deployment.yaml.deploy"
fi

# Replace DB_PASSWORD placeholder
if [ -n "$DB_PASSWORD_VAL" ]; then
  sed -i 's|{{DB_PASSWORD}}|'"$DB_PASSWORD_VAL"'|g' "$K8S_DIR/deployment.yaml.deploy"
else
  sed -i 's|{{DB_PASSWORD}}||g' "$K8S_DIR/deployment.yaml.deploy"
fi

echo "Manifests updated."

# -------------------------------------------------------
# Apply Kubernetes manifests in order
# -------------------------------------------------------
echo ""
echo "Applying Kubernetes manifests..."

echo "  [1/4] Applying namespace..."
kubectl apply -f "$K8S_DIR/namespace.yaml"

echo "  [2/4] Applying deployment..."
kubectl apply -f "$K8S_DIR/deployment.yaml.deploy"

echo "  [3/4] Applying service..."
kubectl apply -f "$K8S_DIR/service.yaml"

echo "  [4/4] Applying ingress..."
kubectl apply -f "$K8S_DIR/ingress.yaml"

# Clean up temporary file
rm -f "$K8S_DIR/deployment.yaml.deploy"

# -------------------------------------------------------
# Wait for rollout
# -------------------------------------------------------
echo ""
echo "Waiting for deployment rollout to complete..."
kubectl rollout status deployment/"$APP_NAME" -n "$NAMESPACE" --timeout=300s
echo "Deployment rollout complete."

# -------------------------------------------------------
# Verify resources
# -------------------------------------------------------
echo ""
echo "Verifying deployed resources in namespace: $NAMESPACE"
kubectl get pods,svc,ingress -n "$NAMESPACE"

# -------------------------------------------------------
# Display application URL
# -------------------------------------------------------
echo ""
echo "Fetching application ingress URL..."
INGRESS_HOST=$(kubectl get ingress "$APP_NAME-ingress" -n "$NAMESPACE" \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")

if [ -n "$INGRESS_HOST" ]; then
  echo ""
  echo "=============================================="
  echo "  Application is accessible at:"
  echo "  http://$INGRESS_HOST"
  echo "  Health Check: http://$INGRESS_HOST/appinfo/health"
  echo "=============================================="
else
  echo "INFO: Ingress hostname not yet assigned. Run the following to check:"
  echo "  kubectl get ingress $APP_NAME-ingress -n $NAMESPACE"
fi

echo ""
echo "--- Rollback Instructions ---"
echo "To rollback to the previous deployment, run:"
echo "  kubectl rollout undo deployment/$APP_NAME -n $NAMESPACE"
echo ""
echo "=============================================="
echo "  Deployment Complete!"
echo "=============================================="
