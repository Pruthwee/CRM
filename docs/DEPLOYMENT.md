# CRM Application - Deployment Guide

## Overview

This guide covers the complete deployment process for the **CRM Spring Boot Application** on **AWS EKS (Elastic Kubernetes Service)**.

- **Application**: CRM (Customer Relationship Management)
- **Framework**: Spring Boot 1.5.10 (Java 8)
- **Build Tool**: Maven
- **Package**: JAR
- **Port**: 8080
- **Health Endpoint**: `/actuator/health`
- **Target Platform**: AWS EKS

---

## Prerequisites

### Local Development
- Docker Desktop 20.x or later
- Java 8 (JDK)
- Maven 3.6+

### AWS EKS Deployment
- AWS CLI v2 configured with appropriate IAM permissions
- `kubectl` v1.24+
- `eksctl` (optional, for cluster creation)
- An existing AWS EKS cluster
- AWS Load Balancer Controller installed on the cluster (for Ingress)

### Required IAM Permissions
- `ecr:GetAuthorizationToken`
- `ecr:BatchCheckLayerAvailability`
- `ecr:PutImage`
- `ecr:InitiateLayerUpload`
- `ecr:UploadLayerPart`
- `ecr:CompleteLayerUpload`
- `ecr:CreateRepository`
- `eks:DescribeCluster`

---

## Project Structure

```
Test10_9/
├── Dockerfile                  # Multi-stage Docker build
├── .dockerignore               # Docker build exclusions
├── docker-compose.yml          # Local development compose
├── pom.xml                     # Maven build descriptor
├── src/                        # Application source code
├── kubernetes/
│   ├── namespace.yaml          # Kubernetes namespace
│   ├── deployment.yaml         # Kubernetes deployment
│   ├── service.yaml            # Kubernetes service (ClusterIP)
│   └── ingress.yaml            # AWS ALB Ingress
├── scripts/
│   ├── build-push.sh           # Linux/macOS build & push
│   ├── build-push.bat          # Windows build & push
│   ├── deploy-image.sh         # Linux/macOS EKS deploy
│   └── deploy-image.bat        # Windows EKS deploy
└── docs/
    └── DEPLOYMENT.md           # This file
```

---

## Local Development with Docker Compose

### 1. Configure Environment Variables

Create a `.env` file in the project root:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/crm?useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

### 2. Build and Start the Application

```bash
docker-compose up --build
```

### 3. Access the Application

- Application: http://localhost:8080
- Health Check: http://localhost:8080/actuator/health

### 4. Stop the Application

```bash
docker-compose down
```

---

## Building and Pushing the Docker Image

### Linux/macOS

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

### Windows

```cmd
scripts\build-push.bat
```

The script will prompt you to:
1. Enter an image tag (defaults to `latest`)
2. Select registry type (AWS ECR or Docker Hub)
3. Provide registry credentials

### Manual Docker Build

```bash
# Build the image
docker build -t crm:latest .

# Tag for ECR
docker tag crm:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/crm:latest

# Push to ECR
aws ecr get-login-password --region <REGION> | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/crm:latest
```

---

## AWS EKS Deployment

### Step 1: Configure AWS CLI

```bash
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region, Output format
```

### Step 2: Configure kubectl for EKS

```bash
aws eks update-kubeconfig --region <AWS_REGION> --name <CLUSTER_NAME>
kubectl cluster-info
```

### Step 3: Run the Deployment Script

#### Linux/macOS

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

#### Windows

```cmd
scripts\deploy-image.bat
```

The script will prompt for:
- AWS Region
- EKS Cluster Name
- Full Docker image URI
- Database connection details

### Step 4: Manual Kubernetes Deployment

If you prefer to deploy manually:

```bash
# 1. Update deployment.yaml with your image URI
sed -i 's|{{IMAGE_URI}}|<YOUR_IMAGE_URI>|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_URL}}|jdbc:mysql://host:3306/crm|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_USERNAME}}|root|g' kubernetes/deployment.yaml
sed -i 's|{{SPRING_DATASOURCE_PASSWORD}}|password|g' kubernetes/deployment.yaml

# 2. Apply manifests in order
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# 3. Wait for rollout
kubectl rollout status deployment/crm -n crm

# 4. Verify
kubectl get pods,svc,ingress -n crm
```

---

## Kubernetes Manifest Descriptions

### namespace.yaml
Creates the `crm` namespace to isolate application resources.

### deployment.yaml
- **Replicas**: 2 (high availability)
- **Image**: Configurable via `{{IMAGE_URI}}` placeholder
- **Resources**: 250m CPU / 512Mi memory (requests), 500m CPU / 1Gi memory (limits)
- **Liveness Probe**: GET `/actuator/health` every 30s (starts after 60s)
- **Readiness Probe**: GET `/actuator/health` every 15s (starts after 30s)
- **JVM Options**: `-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`

### service.yaml
- **Type**: ClusterIP (internal cluster access)
- **Port**: 80 → 8080 (container port)

### ingress.yaml
- **Class**: AWS ALB (Application Load Balancer)
- **Scheme**: internet-facing
- **Host**: `crm.example.com` (update to your domain)

---

## Configuration Management

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `jdbc:mysql://db-host:3306/crm` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | JPA DDL mode | `update` |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` |
| `TZ` | Timezone | `UTC` |

### Using Kubernetes Secrets for Sensitive Data

```bash
# Create a secret for database credentials
kubectl create secret generic crm-db-secret \
  --from-literal=username=root \
  --from-literal=password=your_password \
  -n crm
```

Then reference in deployment.yaml:
```yaml
env:
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: crm-db-secret
        key: password
```

---

## Scaling and Management

### Horizontal Pod Autoscaling

```bash
kubectl autoscale deployment crm \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n crm
```

### Rolling Updates

```bash
# Update image
kubectl set image deployment/crm crm=<NEW_IMAGE_URI> -n crm

# Monitor rollout
kubectl rollout status deployment/crm -n crm
```

### Rollback

```bash
kubectl rollout undo deployment/crm -n crm
```

### Scale Manually

```bash
kubectl scale deployment crm --replicas=3 -n crm
```

---

## Troubleshooting

### Pod Not Starting

```bash
# Check pod status
kubectl get pods -n crm

# Describe pod for events
kubectl describe pod <POD_NAME> -n crm

# Check logs
kubectl logs <POD_NAME> -n crm
kubectl logs <POD_NAME> -n crm --previous  # for crashed pods
```

### Health Check Failures

The application uses Spring Boot Actuator at `/actuator/health`. Ensure:
1. The database is reachable from the pod
2. `management.security.enabled=false` is set (already configured)
3. `management.context-path=/actuator` is set (already configured)
4. The pod has sufficient startup time (initialDelaySeconds: 60)

```bash
# Test health endpoint from within the cluster
kubectl exec -it <POD_NAME> -n crm -- sh
# Then: wget -qO- http://localhost:8080/actuator/health
```

### Database Connection Issues

```bash
# Verify environment variables
kubectl exec -it <POD_NAME> -n crm -- env | grep SPRING_DATASOURCE

# Check if database host is reachable
kubectl exec -it <POD_NAME> -n crm -- sh -c "nc -zv db-host 3306"
```

### Ingress Not Working

```bash
# Check ingress status
kubectl describe ingress crm-ingress -n crm

# Verify AWS Load Balancer Controller is installed
kubectl get pods -n kube-system | grep aws-load-balancer

# Check ALB events
kubectl get events -n crm
```

### Image Pull Errors

```bash
# Verify ECR access
aws ecr describe-repositories --region <REGION>

# Check if image exists
aws ecr list-images --repository-name crm --region <REGION>

# Ensure EKS node IAM role has ECR pull permissions
```

---

## Security Considerations

1. **Non-root container**: The application runs as `appuser` (non-root)
2. **Secrets management**: Use Kubernetes Secrets or AWS Secrets Manager for sensitive data
3. **Network policies**: Consider adding Kubernetes NetworkPolicies to restrict pod communication
4. **Image scanning**: Enable ECR image scanning for vulnerability detection
5. **RBAC**: Apply least-privilege RBAC policies for service accounts
6. **TLS**: Configure HTTPS on the ALB Ingress using ACM certificates:
   ```yaml
   annotations:
     alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:region:account:certificate/cert-id
     alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
   ```

---

## Java-Specific Notes

- **Java Version**: Java 8 (eclipse-temurin:8-jdk runtime)
- **Spring Boot**: 1.5.10.RELEASE
- **JVM Container Support**: `-XX:+UseContainerSupport` ensures JVM respects container memory limits
- **MaxRAMPercentage**: Set to 75% to leave headroom for non-heap memory
- **Startup Time**: Spring Boot 1.5.x may take 30-60 seconds to start; liveness probe has 60s initial delay
- **Actuator**: Spring Boot 1.5.x uses `/actuator` context path (configured via `management.context-path=/actuator`)
- **Security**: Actuator health endpoint is permitted without authentication for Kubernetes probes

---

## Support

For issues with this deployment, check:
1. Application logs: `kubectl logs -f deployment/crm -n crm`
2. Kubernetes events: `kubectl get events -n crm --sort-by='.lastTimestamp'`
3. AWS CloudWatch Logs (if Container Insights enabled)
