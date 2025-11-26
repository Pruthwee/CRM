# CRM Application - Deployment Guide for AWS EKS

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Building Docker Images](#building-docker-images)
4. [AWS EKS Prerequisites](#aws-eks-prerequisites)
5. [EKS Cluster Setup](#eks-cluster-setup)
6. [Deploying to EKS](#deploying-to-eks)
7. [Configuration Management](#configuration-management)
8. [Monitoring and Health Checks](#monitoring-and-health-checks)
9. [Scaling and Updates](#scaling-and-updates)
10. [Troubleshooting](#troubleshooting)
11. [Security Considerations](#security-considerations)

---

## Prerequisites

### Required Software
- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **AWS CLI**: Version 2.x
- **kubectl**: Version 1.24 or higher
- **eksctl**: Version 0.140 or higher (optional, for cluster creation)
- **Git**: For version control
- **Java 8 JDK**: For local development
- **Maven 3.6+**: For building the application

### AWS Account Requirements
- Active AWS account with appropriate permissions
- IAM user or role with EKS, ECR, EC2, and VPC permissions
- AWS credentials configured locally

### System Requirements
- **OS**: Linux, macOS, or Windows 10/11
- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: At least 10GB free

---

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd crm-application
```

### 2. Build the Application Locally
```bash
# Using Maven
mvn clean package -DskipTests

# The JAR file will be created in target/crm-0.0.1-SNAPSHOT.jar
```

### 3. Run with Docker Compose

**Important**: The docker-compose.yml contains only the application service. You need to provide external database connectivity.

```bash
# Set environment variables
export DB_HOST=your-database-host
export DB_PORT=3306
export DB_NAME=crm
export DB_USER=your-db-user
export DB_PASSWORD=your-db-password
export DDL_AUTO=validate

# Start the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

### 4. Access the Application
- **Application URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/appinfo/health
- **Actuator Info**: http://localhost:8080/appinfo/info

---

## Building Docker Images

### Option 1: Using build-push.sh (Linux/macOS)

```bash
cd scripts
chmod +x build-push.sh
./build-push.sh
```

The script will prompt you for:
1. Registry type (AWS ECR or Docker Hub)
2. Registry credentials and configuration
3. Image tag (defaults to 'latest')

### Option 2: Using build-push.bat (Windows)

```cmd
cd scripts
build-push.bat
```

Follow the interactive prompts to configure registry and build settings.

### Manual Docker Build

```bash
# Build the image
docker build -t crm-app:latest .

# Tag for ECR
docker tag crm-app:latest 123456789012.dkr.ecr.us-east-1.amazonaws.com/crm:latest

# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-east-1.amazonaws.com
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/crm:latest
```

---

## AWS EKS Prerequisites

### 1. Install AWS CLI

```bash
# Linux/macOS
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Verify installation
aws --version
```

### 2. Configure AWS Credentials

```bash
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region, Output format
```

### 3. Install kubectl

```bash
# Linux
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# macOS
brew install kubectl

# Windows (using Chocolatey)
choco install kubernetes-cli

# Verify installation
kubectl version --client
```

### 4. Install eksctl (Optional)

```bash
# Linux/macOS
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin

# Verify installation
eksctl version
```

### 5. Install AWS Load Balancer Controller

The AWS Load Balancer Controller is required for Ingress to work with ALB.

```bash
# Add Helm repository
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<your-cluster-name> \
  --set serviceAccount.create=true \
  --set serviceAccount.name=aws-load-balancer-controller
```

---

## EKS Cluster Setup

### Option 1: Create Cluster with eksctl

```bash
# Create a basic EKS cluster
eksctl create cluster \
  --name crm-cluster \
  --region us-east-1 \
  --nodegroup-name crm-nodes \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 2 \
  --nodes-max 4 \
  --managed

# Verify cluster
kubectl get nodes
```

### Option 2: Use Existing EKS Cluster

```bash
# Configure kubectl to use existing cluster
aws eks update-kubeconfig --region us-east-1 --name your-cluster-name

# Verify connectivity
kubectl cluster-info
kubectl get nodes
```

---

## Deploying to EKS

### 1. Prepare Database

Set up an external MySQL database (RDS, Aurora, or self-managed):

```bash
# Create RDS MySQL instance (example)
aws rds create-db-instance \
  --db-instance-identifier crm-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password YourPassword123 \
  --allocated-storage 20
```

### 2. Update Kubernetes Secrets

Before deploying, update the database credentials in `kubernetes/deployment.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: crm-db-secret
  namespace: crm
type: Opaque
stringData:
  username: "your-db-username"
  password: "your-db-password"
```

Or create the secret manually:

```bash
kubectl create secret generic crm-db-secret \
  --from-literal=username=admin \
  --from-literal=password=YourPassword123 \
  -n crm
```

### 3. Run Deployment Script

**Linux/macOS:**
```bash
cd scripts
chmod +x deploy-image.sh
./deploy-image.sh
```

**Windows:**
```cmd
cd scripts
deploy-image.bat
```

The script will prompt you for:
1. AWS region
2. EKS cluster name
3. Docker image URI
4. Database host, port, and name
5. Hibernate DDL mode

### 4. Verify Deployment

```bash
# Check namespace
kubectl get namespaces | grep crm

# Check pods
kubectl get pods -n crm

# Check services
kubectl get svc -n crm

# Check ingress
kubectl get ingress -n crm

# View pod logs
kubectl logs -f deployment/crm-app -n crm
```

### 5. Access the Application

```bash
# Get the ALB DNS name
kubectl get ingress crm-ingress -n crm -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

# Access the application
curl http://<alb-dns-name>/appinfo/health
```

---

## Configuration Management

### Environment Variables

The application uses the following environment variables:

| Variable | Description | Default |
|----------|-------------|----------|
| `DB_HOST` | Database hostname | localhost |
| `DB_PORT` | Database port | 3306 |
| `DB_NAME` | Database name | crm |
| `DB_USER` | Database username | root |
| `DB_PASSWORD` | Database password | password |
| `DDL_AUTO` | Hibernate DDL mode | validate |
| `SPRING_PROFILES_ACTIVE` | Spring profile | production |
| `JAVA_OPTS` | JVM options | -Xmx512m -Xms256m |

### Using ConfigMaps

```bash
# Create ConfigMap from file
kubectl create configmap crm-config \
  --from-file=application.properties \
  -n crm

# Update ConfigMap
kubectl edit configmap crm-config -n crm
```

### Using Secrets

```bash
# Create secret for database credentials
kubectl create secret generic crm-db-secret \
  --from-literal=username=dbuser \
  --from-literal=password=dbpass \
  -n crm

# View secrets (base64 encoded)
kubectl get secret crm-db-secret -n crm -o yaml
```

---

## Monitoring and Health Checks

### Health Check Endpoints

- **Liveness Probe**: `/appinfo/health` (checks if app is running)
- **Readiness Probe**: `/appinfo/health` (checks if app is ready to serve traffic)

### Viewing Logs

```bash
# Stream logs from all pods
kubectl logs -f deployment/crm-app -n crm

# View logs from specific pod
kubectl logs <pod-name> -n crm

# View previous pod logs (if crashed)
kubectl logs <pod-name> -n crm --previous
```

### Monitoring with kubectl

```bash
# Get pod resource usage
kubectl top pods -n crm

# Get node resource usage
kubectl top nodes

# Watch pod status
kubectl get pods -n crm -w
```

### Application Metrics

Spring Boot Actuator provides various metrics endpoints:

- Metrics: `http://<app-url>/appinfo/metrics`
- Health: `http://<app-url>/appinfo/health`
- Info: `http://<app-url>/appinfo/info`

---

## Scaling and Updates

### Horizontal Pod Autoscaling

```bash
# Create HPA
kubectl autoscale deployment crm-app \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n crm

# View HPA status
kubectl get hpa -n crm
```

### Manual Scaling

```bash
# Scale to 5 replicas
kubectl scale deployment crm-app --replicas=5 -n crm

# Verify scaling
kubectl get pods -n crm
```

### Rolling Updates

```bash
# Update image
kubectl set image deployment/crm-app \
  crm-app=123456789012.dkr.ecr.us-east-1.amazonaws.com/crm:v2.0 \
  -n crm

# Watch rollout status
kubectl rollout status deployment/crm-app -n crm

# View rollout history
kubectl rollout history deployment/crm-app -n crm
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/crm-app -n crm

# Rollback to specific revision
kubectl rollout undo deployment/crm-app --to-revision=2 -n crm
```

---

## Troubleshooting

### Pod Issues

#### Pods Not Starting

```bash
# Describe pod to see events
kubectl describe pod <pod-name> -n crm

# Check pod logs
kubectl logs <pod-name> -n crm

# Check events
kubectl get events -n crm --sort-by='.lastTimestamp'
```

**Common Issues:**
- Image pull errors: Check ECR permissions and image URI
- CrashLoopBackOff: Check application logs for errors
- Database connection: Verify DB_HOST and credentials

#### Database Connection Issues

```bash
# Test database connectivity from pod
kubectl exec -it <pod-name> -n crm -- bash
# Inside pod:
apt-get update && apt-get install -y telnet
telnet <db-host> 3306
```

### Service Issues

```bash
# Check service endpoints
kubectl get endpoints -n crm

# Test service from within cluster
kubectl run -it --rm debug --image=busybox --restart=Never -- wget -O- http://crm-service.crm.svc.cluster.local
```

### Ingress Issues

```bash
# Describe ingress
kubectl describe ingress crm-ingress -n crm

# Check AWS Load Balancer Controller logs
kubectl logs -n kube-system deployment/aws-load-balancer-controller

# Verify ALB creation in AWS Console
aws elbv2 describe-load-balancers --region us-east-1
```

### Performance Issues

```bash
# Check resource usage
kubectl top pods -n crm

# Increase resource limits in deployment.yaml
resources:
  requests:
    cpu: "500m"
    memory: "1Gi"
  limits:
    cpu: "1000m"
    memory: "2Gi"
```

---

## Security Considerations

### 1. Use Secrets for Sensitive Data

Never hardcode passwords or API keys in manifests. Use Kubernetes Secrets or AWS Secrets Manager.

### 2. Network Policies

Implement network policies to restrict pod-to-pod communication:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: crm-network-policy
  namespace: crm
spec:
  podSelector:
    matchLabels:
      app: crm-app
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: crm
    ports:
    - protocol: TCP
      port: 8080
```

### 3. RBAC Configuration

Create service accounts with minimal permissions:

```bash
kubectl create serviceaccount crm-sa -n crm
kubectl create rolebinding crm-binding \
  --clusterrole=view \
  --serviceaccount=crm:crm-sa \
  -n crm
```

### 4. Image Scanning

Scan Docker images for vulnerabilities:

```bash
# Using AWS ECR scanning
aws ecr start-image-scan \
  --repository-name crm \
  --image-id imageTag=latest \
  --region us-east-1
```

### 5. Pod Security Standards

Enable Pod Security Standards in the namespace:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: crm
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
```

### 6. TLS/HTTPS Configuration

Configure TLS for Ingress:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: crm-ingress
  namespace: crm
  annotations:
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:region:account:certificate/xxx
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
    alb.ingress.kubernetes.io/ssl-redirect: '443'
spec:
  # ... rest of ingress config
```

---

## Technology-Specific Notes

### Spring Boot 1.5.10 Considerations

1. **Actuator Endpoints**: This version uses `/appinfo` context path for management endpoints
2. **Security**: Management security is disabled in properties. Enable for production.
3. **Thymeleaf**: Uses LEGACYHTML5 mode with nekohtml parser
4. **Database**: Supports both H2 (development) and MySQL (production)

### JVM Tuning for Containers

The application uses the following JVM flags optimized for containers:

```bash
-Xmx512m                    # Maximum heap size
-Xms256m                    # Initial heap size
-XX:+UseContainerSupport    # Enable container awareness
-XX:MaxRAMPercentage=75.0   # Use 75% of container memory
```

Adjust these based on your workload:
- For memory-intensive operations: Increase Xmx to 1024m or higher
- For high-throughput: Consider using G1GC: `-XX:+UseG1GC`

### Database Migration

For production deployments, consider using Flyway or Liquibase for database migrations instead of Hibernate DDL auto.

---

## Support and Additional Resources

- **AWS EKS Documentation**: https://docs.aws.amazon.com/eks/
- **Kubernetes Documentation**: https://kubernetes.io/docs/
- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/1.5.x/reference/html/
- **Docker Documentation**: https://docs.docker.com/

---

**Deployment Guide Version**: 1.0  
**Last Updated**: 2025-11-26  
**Application**: CRM Spring Boot Application  
**Target Platform**: AWS EKS