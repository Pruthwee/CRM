# CRM Application - Deployment Guide

## Overview

This guide covers the complete deployment process for the **CRM** Spring Boot application on **AWS EKS (Elastic Kubernetes Service)**.

- **Application**: CRM (Customer Relationship Management)
- **Framework**: Spring Boot 1.5.10 (Java 8)
- **Build Tool**: Maven
- **Package**: JAR
- **Application Port**: 8080
- **Health Endpoint**: `/appinfo/health`
- **Target Platform**: AWS EKS

---

## Prerequisites

### Local Development Tools
- **Java 8** (JDK 1.8+)
- **Maven 3.6+**
- **Docker** (20.10+)
- **Docker Compose** (v2+)

### AWS & Kubernetes Tools
- **AWS CLI** (v2) — [Install Guide](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
- **kubectl** — [Install Guide](https://kubernetes.io/docs/tasks/tools/)
- **eksctl** (optional, for cluster creation) — [Install Guide](https://eksctl.io/)
- **AWS IAM Permissions**: ECR (push/pull), EKS (cluster access), EC2 (load balancer)

---

## Project Structure

```
TEST/
├── Dockerfile                  # Multi-stage Docker build
├── docker-compose.yml          # Local development compose
├── .dockerignore               # Docker build exclusions
├── pom.xml                     # Maven build configuration
├── src/                        # Application source code
├── kubernetes/
│   ├── namespace.yaml          # Kubernetes namespace
│   ├── deployment.yaml         # Application deployment
│   ├── service.yaml            # ClusterIP service
│   └── ingress.yaml            # AWS ALB ingress
├── scripts/
│   ├── build-push.sh           # Linux/macOS build & push
│   ├── build-push.bat          # Windows build & push
│   ├── deploy-image.sh         # Linux/macOS EKS deploy
│   └── deploy-image.bat        # Windows EKS deploy
└── docs/
    └── DEPLOYMENT.md           # This file
```

---

## Local Development Setup

### 1. Build the Application Locally

```bash
mvn clean package -DskipTests
```

### 2. Run with Docker Compose

Create a `.env` file in the project root:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/crm?useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

Start the application:

```bash
docker-compose up --build
```

Access the application at: `http://localhost:8080`

Health check: `http://localhost:8080/appinfo/health`

### 3. Stop the Application

```bash
docker-compose down
```

---

## Docker Build & Push

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
# Build
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

### Step 2: Create or Connect to EKS Cluster

**Create a new cluster (if needed):**

```bash
eksctl create cluster \
  --name crm-cluster \
  --region us-east-1 \
  --nodegroup-name crm-nodes \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4
```

**Connect to existing cluster:**

```bash
aws eks update-kubeconfig --region us-east-1 --name crm-cluster
kubectl cluster-info
```

### Step 3: Install AWS Load Balancer Controller (for Ingress)

```bash
# Add EKS Helm repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install AWS Load Balancer Controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=crm-cluster \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### Step 4: Deploy Using Script

**Linux/macOS:**

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

**Windows:**

```cmd
scripts\deploy-image.bat
```

The script will prompt for:
- AWS Region
- EKS Cluster Name
- Docker Image URI (full path with tag)
- Database connection details

### Step 5: Manual Kubernetes Deployment

If you prefer manual deployment, update `kubernetes/deployment.yaml` with your image URI and environment values, then:

```bash
# Apply manifests in order
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/crm -n crm

# Verify
kubectl get pods,svc,ingress -n crm
```

---

## Configuration Management

### Environment Variables

| Variable | Description | Default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/crm?useSSL=false` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL mode | `update` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `docker` |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` |
| `TZ` | Timezone | `UTC` |

### JVM Configuration

The application uses the following JVM flags for container optimization:

```
-Xmx512m                    # Max heap size
-Xms256m                    # Initial heap size
-XX:+UseContainerSupport    # Enable container memory awareness
-XX:MaxRAMPercentage=75.0   # Use 75% of container memory for heap
-Djava.security.egd=file:/dev/./urandom  # Faster random number generation
```

---

## Scaling & Management

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

# Rollback to specific revision
kubectl rollout undo deployment/crm --to-revision=2 -n crm
```

### Scale Replicas

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
kubectl describe pod <pod-name> -n crm

# Check logs
kubectl logs <pod-name> -n crm
kubectl logs -l app=crm -n crm --tail=100
```

### Common Issues

**1. Database Connection Failure**
- Verify `SPRING_DATASOURCE_URL` is correct and reachable from the pod
- Check security groups allow traffic on port 3306
- Verify credentials are correct

**2. OOMKilled (Out of Memory)**
- Increase memory limits in `kubernetes/deployment.yaml`
- Adjust `JAVA_OPTS` heap settings (`-Xmx`)

**3. Liveness/Readiness Probe Failures**
- Spring Boot 1.5.x uses `/appinfo/health` (management.context-path=/appinfo)
- Check `management.security.enabled=false` is set
- Increase `initialDelaySeconds` if JVM startup is slow

**4. Ingress Not Working**
- Verify AWS Load Balancer Controller is installed
- Check ingress annotations match your ALB controller version
- Verify security groups allow HTTP/HTTPS traffic

### Useful Commands

```bash
# Get all resources in namespace
kubectl get all -n crm

# Watch pods
kubectl get pods -n crm -w

# Execute shell in pod
kubectl exec -it <pod-name> -n crm -- /bin/sh

# Port-forward for local testing
kubectl port-forward svc/crm-service 8080:80 -n crm

# View ingress details
kubectl describe ingress crm-ingress -n crm
```

---

## Security Considerations

1. **Non-root Container**: The application runs as `appuser` (non-root) inside the container
2. **Secrets Management**: Use Kubernetes Secrets or AWS Secrets Manager for sensitive values (DB passwords, API keys)
3. **Network Policies**: Consider adding Kubernetes NetworkPolicies to restrict pod-to-pod communication
4. **Image Scanning**: Enable ECR image scanning for vulnerability detection
5. **RBAC**: Apply least-privilege IAM roles for EKS node groups
6. **TLS**: Configure HTTPS on the ALB ingress using ACM certificates:
   ```yaml
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:region:account:certificate/cert-id
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
   ```

---

## Technology-Specific Notes

### Spring Boot 1.5.x on Java 8

- **Actuator**: Uses legacy endpoint paths. Health endpoint is at `/appinfo/health` (configured via `management.context-path=/appinfo`)
- **Security**: `management.security.enabled=false` disables actuator security for health probes
- **Thymeleaf**: Uses `LEGACYHTML5` mode with NekoHTML parser
- **Database**: Configured for MySQL with H2 as fallback for testing
- **JPA**: Uses `create-drop` in development; set to `update` or `validate` in production

### Database Considerations

- The application requires a MySQL database. Ensure your RDS or MySQL instance is accessible from the EKS cluster
- For production, use `spring.jpa.hibernate.ddl-auto=validate` or `none`
- Consider using AWS RDS MySQL with a private subnet and security group allowing EKS node traffic

### PDF & CSV Features

- The application includes PDF generation (iTextPDF, PDFBox) and CSV import/export (OpenCSV, SuperCSV)
- These features are CPU-intensive; monitor resource usage and adjust limits accordingly
