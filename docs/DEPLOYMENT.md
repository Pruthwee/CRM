# CRM-Testing - Deployment Guide

## Overview

This guide covers the complete deployment process for the **CRM-Testing** application — a Spring Boot 2.7.18 CRM web application built with Java 17, Maven, Spring Security, Spring Data JPA, Thymeleaf, and Spring Boot Actuator. The application is containerized using Docker and deployed to **AWS EKS (Elastic Kubernetes Service)**.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Local Development with Docker Compose](#local-development-with-docker-compose)
4. [Build and Push Docker Image](#build-and-push-docker-image)
5. [AWS EKS Prerequisites](#aws-eks-prerequisites)
6. [EKS Cluster Setup](#eks-cluster-setup)
7. [Kubernetes Deployment](#kubernetes-deployment)
8. [Automated Deployment Script](#automated-deployment-script)
9. [Health Checks and Monitoring](#health-checks-and-monitoring)
10. [Scaling and Management](#scaling-and-management)
11. [Configuration Management](#configuration-management)
12. [Troubleshooting](#troubleshooting)
13. [Security Considerations](#security-considerations)
14. [Java-Specific Notes](#java-specific-notes)

---

## Prerequisites

### Local Development Tools
- **Docker** 20.10+ and **Docker Compose** v2+
- **Java 17** (for local builds)
- **Maven 3.9+** (for local builds)
- **Git**

### AWS & Kubernetes Tools
- **AWS CLI** v2+ — [Install Guide](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
- **kubectl** v1.27+ — [Install Guide](https://kubernetes.io/docs/tasks/tools/)
- **eksctl** (optional, for cluster creation) — [Install Guide](https://eksctl.io/)
- **AWS IAM permissions**: ECR (push/pull), EKS (cluster access), EC2 (load balancer)

---

## Project Structure

```
CRM-testing/
├── Dockerfile                    # Multi-stage Docker build
├── docker-compose.yml            # Local development compose file
├── .dockerignore                 # Docker build exclusions
├── pom.xml                       # Maven build descriptor
├── src/
│   └── main/
│       ├── java/crm/             # Application source code
│       └── resources/
│           └── application.properties  # Application configuration
├── kubernetes/
│   ├── namespace.yaml            # Kubernetes namespace
│   ├── deployment.yaml           # Kubernetes deployment
│   ├── service.yaml              # Kubernetes service (ClusterIP)
│   └── ingress.yaml              # AWS ALB Ingress
├── scripts/
│   ├── build-push.sh             # Linux/macOS build & push script
│   ├── build-push.bat            # Windows build & push script
│   ├── deploy-image.sh           # Linux/macOS EKS deploy script
│   └── deploy-image.bat          # Windows EKS deploy script
└── docs/
    └── DEPLOYMENT.md             # This guide
```

---

## Local Development with Docker Compose

### 1. Build and Start the Application

```bash
# From the project root directory
docker compose up --build
```

The application will be available at: **http://localhost:8080**

### 2. Access the Application

| URL | Description |
|-----|-------------|
| `http://localhost:8080` | Application home page |
| `http://localhost:8080/login` | Login page |
| `http://localhost:8080/appinfo/health` | Health check endpoint |
| `http://localhost:8080/h2-console` | H2 in-memory database console |

### 3. Environment Variables for Docker Compose

Create a `.env` file in the project root to override defaults:

```env
DB_URL=jdbc:h2:mem:crmdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DB_USERNAME=sa
DB_PASSWORD=
SPRING_PROFILES_ACTIVE=docker
```

For MySQL (external database):
```env
DB_URL=jdbc:mysql://your-mysql-host:3306/crmdb?useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=crmuser
DB_PASSWORD=yourpassword
```

### 4. Stop the Application

```bash
docker compose down
```

---

## Build and Push Docker Image

### Linux / macOS

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

### Windows

```cmd
scripts\build-push.bat
```

### Script Prompts

The script will interactively ask for:
1. **Image tag** (default: `latest`)
2. **Registry type**: AWS ECR or Docker Hub
3. **Registry credentials** based on selection

### Manual Docker Build

```bash
# Build the image
docker build -t crm-testing:latest .

# Tag for ECR
docker tag crm-testing:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/crm-testing:latest

# Push to ECR
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/crm-testing:latest
```

---

## AWS EKS Prerequisites

### 1. Configure AWS CLI

```bash
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region, Output format
```

### 2. Verify AWS Identity

```bash
aws sts get-caller-identity
```

### 3. Required IAM Permissions

Ensure your IAM user/role has the following policies:
- `AmazonEKSClusterPolicy`
- `AmazonEKSWorkerNodePolicy`
- `AmazonEC2ContainerRegistryFullAccess`
- `ElasticLoadBalancingFullAccess`

---

## EKS Cluster Setup

### Option A: Use Existing Cluster

```bash
# Configure kubectl to use your existing EKS cluster
aws eks update-kubeconfig --region <AWS_REGION> --name <CLUSTER_NAME>

# Verify connectivity
kubectl cluster-info
kubectl get nodes
```

### Option B: Create a New EKS Cluster (eksctl)

```bash
eksctl create cluster \
  --name crm-testing-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4 \
  --managed
```

### Install AWS Load Balancer Controller (Required for Ingress)

```bash
# Add the EKS chart repository
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the AWS Load Balancer Controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<CLUSTER_NAME> \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

---

## Kubernetes Deployment

### Manual Step-by-Step Deployment

#### 1. Update the Deployment Image

Edit `kubernetes/deployment.yaml` and replace `{{IMAGE_URI}}` with your actual image URI:

```yaml
image: "123456789.dkr.ecr.us-east-1.amazonaws.com/crm-testing:latest"
```

Also replace environment variable placeholders:
- `{{DB_URL}}` → your database URL
- `{{DB_USERNAME}}` → your database username
- `{{DB_PASSWORD}}` → your database password

#### 2. Apply Manifests in Order

```bash
# 1. Create namespace
kubectl apply -f kubernetes/namespace.yaml

# 2. Deploy the application
kubectl apply -f kubernetes/deployment.yaml

# 3. Create the service
kubectl apply -f kubernetes/service.yaml

# 4. Create the ingress
kubectl apply -f kubernetes/ingress.yaml
```

#### 3. Verify Deployment

```bash
# Check pods
kubectl get pods -n crm-testing

# Check services
kubectl get svc -n crm-testing

# Check ingress
kubectl get ingress -n crm-testing

# View pod logs
kubectl logs -f deployment/crm-testing -n crm-testing
```

#### 4. Get Application URL

```bash
kubectl get ingress crm-testing-ingress -n crm-testing \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

---

## Automated Deployment Script

The deployment scripts handle all steps automatically.

### Linux / macOS

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

### Windows

```cmd
scripts\deploy-image.bat
```

### Script Prompts

| Prompt | Description |
|--------|-------------|
| AWS Region | e.g., `us-east-1` |
| EKS Cluster Name | Your EKS cluster name |
| Docker Image URI | Full image path with tag |
| DB_URL | Database connection URL (optional) |
| DB_USERNAME | Database username (optional) |
| DB_PASSWORD | Database password (optional) |

---

## Health Checks and Monitoring

### Health Endpoint

The application uses a **custom Actuator base path**: `/appinfo`

| Endpoint | URL | Description |
|----------|-----|-------------|
| Health | `/appinfo/health` | Application health status |
| Info | `/appinfo/info` | Application info |

> **Note**: The `/appinfo/health` endpoint is publicly accessible (no authentication required) as configured in `SecurityConfig.java`.

### Kubernetes Probes

The deployment is configured with:

```yaml
livenessProbe:
  httpGet:
    path: /appinfo/health
    port: 8080
  initialDelaySeconds: 60    # Allow JVM startup time
  periodSeconds: 30

readinessProbe:
  httpGet:
    path: /appinfo/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 15
```

### Check Application Health

```bash
# Port-forward to test locally
kubectl port-forward deployment/crm-testing 8080:8080 -n crm-testing

# Test health endpoint
curl http://localhost:8080/appinfo/health
```

---

## Scaling and Management

### Manual Scaling

```bash
# Scale to 3 replicas
kubectl scale deployment crm-testing --replicas=3 -n crm-testing
```

### Horizontal Pod Autoscaler (HPA)

```bash
kubectl autoscale deployment crm-testing \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n crm-testing

# Check HPA status
kubectl get hpa -n crm-testing
```

### Rolling Update

```bash
# Update image
kubectl set image deployment/crm-testing \
  crm-testing=<NEW_IMAGE_URI> \
  -n crm-testing

# Monitor rollout
kubectl rollout status deployment/crm-testing -n crm-testing
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/crm-testing -n crm-testing

# Rollback to specific revision
kubectl rollout history deployment/crm-testing -n crm-testing
kubectl rollout undo deployment/crm-testing --to-revision=2 -n crm-testing
```

---

## Configuration Management

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `docker` | Active Spring profile |
| `SERVER_PORT` | `8080` | Application port |
| `DB_URL` | H2 in-memory | Database JDBC URL |
| `DB_USERNAME` | `sa` | Database username |
| `DB_PASSWORD` | `` | Database password |
| `JAVA_OPTS` | JVM flags | JVM tuning options |
| `TZ` | `UTC` | Timezone |

### Using Kubernetes Secrets for Sensitive Data

```bash
# Create a secret for database credentials
kubectl create secret generic crm-db-secret \
  --from-literal=DB_USERNAME=crmuser \
  --from-literal=DB_PASSWORD=yourpassword \
  -n crm-testing
```

Then reference in `deployment.yaml`:

```yaml
env:
  - name: DB_USERNAME
    valueFrom:
      secretKeyRef:
        name: crm-db-secret
        key: DB_USERNAME
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: crm-db-secret
        key: DB_PASSWORD
```

### Using ConfigMaps for Non-Sensitive Config

```bash
kubectl create configmap crm-config \
  --from-literal=SPRING_PROFILES_ACTIVE=production \
  --from-literal=TZ=UTC \
  -n crm-testing
```

---

## Troubleshooting

### Pod Not Starting

```bash
# Check pod status
kubectl get pods -n crm-testing

# Describe pod for events
kubectl describe pod <POD_NAME> -n crm-testing

# View pod logs
kubectl logs <POD_NAME> -n crm-testing

# View previous container logs (if crashed)
kubectl logs <POD_NAME> -n crm-testing --previous
```

### Common Issues

#### 1. ImagePullBackOff
```bash
# Verify ECR credentials
aws ecr get-login-password --region <REGION> | \
  docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com

# Check if image exists in ECR
aws ecr describe-images --repository-name crm-testing --region <REGION>
```

#### 2. CrashLoopBackOff
```bash
# Check application logs
kubectl logs <POD_NAME> -n crm-testing --previous

# Common causes:
# - Database connection failure (check DB_URL, DB_USERNAME, DB_PASSWORD)
# - Insufficient memory (increase memory limits)
# - Missing environment variables
```

#### 3. Health Check Failing
```bash
# Port-forward and test manually
kubectl port-forward deployment/crm-testing 8080:8080 -n crm-testing
curl -v http://localhost:8080/appinfo/health

# Increase initialDelaySeconds if JVM startup is slow
```

#### 4. Ingress Not Getting External IP
```bash
# Check AWS Load Balancer Controller is installed
kubectl get pods -n kube-system | grep aws-load-balancer

# Check ingress events
kubectl describe ingress crm-testing-ingress -n crm-testing
```

#### 5. Out of Memory (OOMKilled)
```bash
# Check resource usage
kubectl top pods -n crm-testing

# Increase memory limits in deployment.yaml
# resources.limits.memory: "2Gi"
# Also adjust JAVA_OPTS: -Xmx1g -Xms512m
```

### Useful Debugging Commands

```bash
# Execute shell in running pod
kubectl exec -it <POD_NAME> -n crm-testing -- /bin/sh

# Check all resources in namespace
kubectl get all -n crm-testing

# Watch pod status in real-time
kubectl get pods -n crm-testing -w

# Check cluster events
kubectl get events -n crm-testing --sort-by='.lastTimestamp'
```

---

## Security Considerations

1. **Non-root container**: The application runs as a non-root user (`appuser`) inside the container.
2. **Secrets management**: Use Kubernetes Secrets (or AWS Secrets Manager) for sensitive data — never hardcode credentials.
3. **Network policies**: Consider adding Kubernetes NetworkPolicies to restrict pod-to-pod communication.
4. **Image scanning**: Enable ECR image scanning to detect vulnerabilities.
5. **RBAC**: Apply least-privilege RBAC policies for the application's service account.
6. **TLS/HTTPS**: Configure HTTPS on the ALB Ingress using ACM certificates:
   ```yaml
   annotations:
     alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:<REGION>:<ACCOUNT>:certificate/<CERT_ID>
     alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
   ```
7. **Spring Security**: The application uses Spring Security with BCrypt password encoding. Ensure strong passwords are used.

---

## Java-Specific Notes

### JVM Configuration

The application is configured with container-aware JVM flags:

```
-Xms256m                          # Initial heap size
-Xmx512m                          # Maximum heap size
-XX:+UseContainerSupport          # Enable container memory awareness
-XX:MaxRAMPercentage=75.0         # Use 75% of container memory for heap
-Djava.security.egd=file:/dev/./urandom  # Faster random number generation
```

### Spring Boot Actuator

The management endpoints are exposed at the custom base path `/appinfo`:
- Health: `GET /appinfo/health`
- Info: `GET /appinfo/info`

These are configured in `application.properties`:
```properties
management.endpoints.web.base-path=/appinfo
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

### Database Configuration

The application defaults to **H2 in-memory database** for development. For production:
1. Set `DB_URL` to a MySQL/PostgreSQL connection string
2. Update `spring.datasource.driver-class-name` if switching from H2
3. The MySQL connector (`mysql-connector-java:8.0.33`) is already included in the classpath

### Spring Profiles

| Profile | Description |
|---------|-------------|
| `docker` | Default for containerized deployments |
| (default) | Uses H2 in-memory database |

### H2 Console

The H2 console is enabled at `/h2-console` for development. **Disable this in production** by setting:
```properties
spring.h2.console.enabled=false
```

### Thymeleaf Cache

Template caching is disabled (`spring.thymeleaf.cache=false`) for development. For production, enable it:
```properties
spring.thymeleaf.cache=true
```

---

## Quick Reference

```bash
# Local development
docker compose up --build

# Build and push image
./scripts/build-push.sh

# Deploy to EKS
./scripts/deploy-image.sh

# Check deployment status
kubectl get pods,svc,ingress -n crm-testing

# View logs
kubectl logs -f deployment/crm-testing -n crm-testing

# Rollback
kubectl rollout undo deployment/crm-testing -n crm-testing

# Scale
kubectl scale deployment crm-testing --replicas=3 -n crm-testing
```
