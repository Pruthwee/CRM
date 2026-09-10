#!/bin/bash
set -e
set -o pipefail

echo "============================================"
echo "  Deploy CRM to AWS EKS"
echo "============================================"
echo ""

# Prompt for AWS region
read -p "Enter AWS Region (e.g. us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "AWS Region is required. Exiting."
  exit 1
fi

# Prompt for EKS cluster name
read -p "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "EKS Cluster Name is required. Exiting."
  exit 1
fi

# Prompt for Docker image URI
read -p "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/crm:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "Docker image URI is required. Exiting."
  exit 1
fi

# Prompt for application environment variables
echo ""
echo "--- Application Environment Variables ---"
echo "(Press Enter to skip any variable)"

read -p "Enter SPRING_DATASOURCE_URL (e.g. jdbc:mysql://host:3306/crm?useSSL=false): " SPRING_DATASOURCE_URL
if [ -z "$SPRING_DATASOURCE_URL" ]; then
  SPRING_DATASOURCE_URL="jdbc:mysql://db-host:3306/crm?useSSL=false"
fi

read -p "Enter SPRING_DATASOURCE_USERNAME (default: root): " SPRING_DATASOURCE_USERNAME
if [ -z "$SPRING_DATASOURCE_USERNAME" ]; then
  SPRING_DATASOURCE_USERNAME="root"
fi

read -sp "Enter SPRING_DATASOURCE_PASSWORD: " SPRING_DATASOURCE_PASSWORD
echo ""
if [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
  SPRING_DATASOURCE_PASSWORD="password"
fi

echo ""
echo "--- Configuring kubectl for EKS ---"
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"
if [ $? -ne 0 ]; then
  echo "Failed to configure kubectl. Exiting."
  exit 1
fi

echo "Verifying cluster connectivity..."
kubectl cluster-info || exit 1

echo ""
echo "--- Updating Kubernetes manifests ---"
# Replace placeholders in deployment.yaml using pipe delimiter
sed -i 's|{{IMAGE_URI}}|'"$IMAGE_URI"'|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_URL}}|'"$SPRING_DATASOURCE_URL"'|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_USERNAME}}|'"$SPRING_DATASOURCE_USERNAME"'|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_PASSWORD}}|'"$SPRING_DATASOURCE_PASSWORD"'|g' kubernetes/deployment.yaml

echo ""
echo "--- Applying Kubernetes manifests ---"
echo "Applying namespace..."
kubectl apply -f kubernetes/namespace.yaml

echo "Applying deployment..."
kubectl apply -f kubernetes/deployment.yaml

echo "Applying service..."
kubectl apply -f kubernetes/service.yaml

echo "Applying ingress..."
kubectl apply -f kubernetes/ingress.yaml

echo ""
echo "--- Waiting for deployment rollout ---"
kubectl rollout status deployment/crm -n crm
if [ $? -ne 0 ]; then
  echo "Deployment rollout failed. Rolling back..."
  kubectl rollout undo deployment/crm -n crm
  exit 1
fi

echo ""
echo "--- Verifying resources ---"
kubectl get pods,svc,ingress -n crm

echo ""
echo "--- Application Access ---"
INGRESS_HOST=$(kubectl get ingress crm-ingress -n crm -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")
echo "Application URL: http://$INGRESS_HOST"
echo ""
echo "============================================"
echo "  Deployment completed successfully!"
echo "============================================"
echo ""
echo "Rollback command (if needed):"
echo "  kubectl rollout undo deployment/crm -n crm"
