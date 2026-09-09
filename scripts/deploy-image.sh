#!/bin/bash
set -e
set -o pipefail

echo "============================================"
echo "  Deploy CRM to AWS EKS"
echo "============================================"
echo ""

# Prompt for AWS configuration
read -p "Enter AWS Region (e.g., us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "AWS Region is required. Exiting."
  exit 1
fi

read -p "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "EKS Cluster Name is required. Exiting."
  exit 1
fi

read -p "Enter Docker Image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/crm:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "Docker Image URI is required. Exiting."
  exit 1
fi

echo ""
echo "--- Application Environment Variables ---"
read -p "Enter SPRING_DATASOURCE_URL (e.g., jdbc:mysql://host:3306/crm?useSSL=false): " SPRING_DATASOURCE_URL
read -p "Enter SPRING_DATASOURCE_USERNAME (default: root): " SPRING_DATASOURCE_USERNAME
if [ -z "$SPRING_DATASOURCE_USERNAME" ]; then SPRING_DATASOURCE_USERNAME="root"; fi
read -sp "Enter SPRING_DATASOURCE_PASSWORD: " SPRING_DATASOURCE_PASSWORD
echo ""
read -p "Enter SPRING_JPA_HIBERNATE_DDL_AUTO (default: update): " SPRING_JPA_HIBERNATE_DDL_AUTO
if [ -z "$SPRING_JPA_HIBERNATE_DDL_AUTO" ]; then SPRING_JPA_HIBERNATE_DDL_AUTO="update"; fi

echo ""
echo "Configuring kubectl for EKS cluster: $CLUSTER_NAME in $AWS_REGION..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"
if [ $? -ne 0 ]; then
  echo "Failed to configure kubectl. Exiting."
  exit 1
fi

echo "Verifying cluster connectivity..."
kubectl cluster-info || exit 1

echo ""
echo "Updating Kubernetes manifests with provided values..."

# Replace placeholders in deployment.yaml
sed -i 's|{{IMAGE_URI}}|'"$IMAGE_URI"'|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_URL}}|'"$SPRING_DATASOURCE_URL"'|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_USERNAME}}|'"$SPRING_DATASOURCE_USERNAME"'|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_PASSWORD}}|'"$SPRING_DATASOURCE_PASSWORD"'|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_JPA_HIBERNATE_DDL_AUTO}}|'"$SPRING_JPA_HIBERNATE_DDL_AUTO"'|g' kubernetes/deployment.yaml

echo ""
echo "Applying Kubernetes manifests..."

echo "  [1/4] Applying namespace..."
kubectl apply -f kubernetes/namespace.yaml

echo "  [2/4] Applying deployment..."
kubectl apply -f kubernetes/deployment.yaml

echo "  [3/4] Applying service..."
kubectl apply -f kubernetes/service.yaml

echo "  [4/4] Applying ingress..."
kubectl apply -f kubernetes/ingress.yaml

echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/crm -n crm --timeout=300s
if [ $? -ne 0 ]; then
  echo "Deployment rollout failed. Rolling back..."
  kubectl rollout undo deployment/crm -n crm
  echo "Rollback complete. Check pod logs: kubectl logs -l app=crm -n crm"
  exit 1
fi

echo ""
echo "Verifying deployed resources..."
kubectl get pods,svc,ingress -n crm

echo ""
echo "============================================"
echo "  Deployment Complete!"
echo "  Namespace: crm"
echo "  Image: $IMAGE_URI"
echo ""
INGRESS_HOST=$(kubectl get ingress crm-ingress -n crm -o jsonpath='{.spec.rules[0].host}' 2>/dev/null || echo "crm.example.com")
echo "  Application URL: http://$INGRESS_HOST"
echo "  Health Check:    http://$INGRESS_HOST/appinfo/health"
echo "============================================"
echo ""
echo "Useful commands:"
echo "  kubectl get pods -n crm"
echo "  kubectl logs -l app=crm -n crm"
echo "  kubectl describe deployment crm -n crm"
echo "  kubectl rollout undo deployment/crm -n crm  # Rollback"
