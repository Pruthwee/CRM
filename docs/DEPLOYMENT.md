# CRM Container - Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Development Setup](#local-development-setup)
4. [Docker Deployment](#docker-deployment)
5. [AWS EKS Deployment](#aws-eks-deployment)
6. [Configuration Management](#configuration-management)
7. [Troubleshooting](#troubleshooting)
8. [Security Considerations](#security-considerations)
9. [Technology-Specific Notes](#technology-specific-notes)

---

## Overview

This is a Spring Boot 1.5.10 application built with Java 8 and Maven. The application is a CRM (Customer Relationship Management) system with the following features:
- Spring Boot web application with Thymeleaf templates
- Spring Security for authentication and authorization
- Spring Data JPA for database access
- MySQL database support
- PDF and CSV export capabilities
- Spring Boot Actuator for monitoring

**Application Details:**
- **Framework:** Spring Boot 1.5.10.RELEASE
- **Java Version:** 1.8
- **Build Tool:** Maven
- **Package Type:** JAR
- **Application Port:** 8080
- **Management Port:** 8081
- **Health Endpoint:** /health
- **Management Context:** /appinfo

---

## Prerequisites

### Required Software

#### For Local Development:
- **Java Development Kit (JDK) 8** or higher
- **Maven 3.6+** for building the application
- **Docker Desktop** (latest version)
- **Docker Compose** (included with Docker Desktop)
- **Git** for version control

#### For AWS EKS Deployment:
- **AWS CLI** (version 2.x or higher)
  ```bash
  # Install AWS CLI
  # macOS
  brew install awscli
  
  # Linux
  curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
  unzip awscliv2.zip
  sudo ./aws/install
  
  # Windows
  # Download and run: https://awscli.amazonaws.com/AWSCLIV2.msi
  ```

- **kubectl** (Kubernetes command-line tool)
  ```bash
  # macOS
  brew install kubectl
  
  # Linux
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
  sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
  
  # Windows
  # Download from: https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
  ```

- **eksctl** (optional, for EKS cluster management)
  ```bash
  # macOS
  brew tap weaveworks/tap
  brew install weaveworks/tap/eksctl
  
  # Linux
  curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
  sudo mv /tmp/eksctl /usr/local/bin
  
  # Windows
  # Download from: https://github.com/weaveworks/eksctl/releases
  ```

#### AWS IAM Permissions Required:
- **ECR (Elastic Container Registry):**
  - `ecr:GetAuthorizationToken`
  - `ecr:CreateRepository`
  - `ecr:DescribeRepositories`
  - `ecr:PutImage`
  - `ecr:InitiateLayerUpload`
  - `ecr:UploadLayerPart`
  - `ecr:CompleteLayerUpload`

- **EKS (Elastic Kubernetes Service):**
  - `eks:DescribeCluster`
  - `eks:ListClusters`
  - `eks:UpdateClusterConfig`

- **EC2 (for Load Balancer):**
  - `ec2:DescribeVpcs`
  - `ec2:DescribeSubnets`
  - `ec2:DescribeSecurityGroups`
  - `ec2:CreateSecurityGroup`
  - `ec2:AuthorizeSecurityGroupIngress`

- **ELB (Elastic Load Balancing):**
  - `elasticloadbalancing:CreateLoadBalancer`
  - `elasticloadbalancing:DescribeLoadBalancers`
  - `elasticloadbalancing:ModifyLoadBalancerAttributes`

---

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd CRM-Container
```

### 2. Build the Application Locally
```bash
# Using Maven
mvn clean package -DskipTests

# The JAR file will be created in target/crm-0.0.1-SNAPSHOT.jar
```

### 3. Run Locally (Without Docker)
```bash
# Set up MySQL database first
# Update application.properties with your database credentials

# Run the application
java -jar target/crm-0.0.1-SNAPSHOT.jar

# Or using Maven
mvn spring-boot:run
```

The application will be available at:
- **Application:** http://localhost:8080
- **Health Check:** http://localhost:8080/health
- **Actuator Info:** http://localhost:8081/appinfo

---

## Docker Deployment

### 1. Build Docker Image Locally
```bash
# Build the image
docker build -t crm-container:latest .

# Verify the image
docker images | grep crm-container
```

### 2. Run with Docker Compose

**Important:** The docker-compose.yml contains only the application service. You need to provide external services (MySQL database) separately.

```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

**Environment Variables:**
Update the `docker-compose.yml` file with your database connection details:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/crm?useSSL=false
  - SPRING_DATASOURCE_USERNAME=your-username
  - SPRING_DATASOURCE_PASSWORD=your-password
```

### 3. Run Docker Container Manually
```bash
docker run -d \
  --name crm-container \
  -p 8080:8080 \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/crm?useSSL=false \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  crm-container:latest
```

---

## AWS EKS Deployment

### Step 1: Prerequisites Setup

#### 1.1 Configure AWS CLI
```bash
# Configure AWS credentials
aws configure

# Enter your:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region (e.g., us-east-1)
# - Default output format (json)

# Verify configuration
aws sts get-caller-identity
```

#### 1.2 Create EKS Cluster (if not exists)

**Option A: Using eksctl (Recommended)**
```bash
# Create a new EKS cluster
eksctl create cluster \
  --name crm-cluster \
  --region us-east-1 \
  --nodegroup-name crm-nodes \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 3 \
  --managed

# This will take 15-20 minutes
```

**Option B: Using AWS Console**
1. Go to AWS EKS Console
2. Click "Create cluster"
3. Follow the wizard to create cluster and node group
4. Wait for cluster to become "Active"

#### 1.3 Install AWS Load Balancer Controller

The AWS Load Balancer Controller is required for the Ingress to work:

```bash
# Download IAM policy
curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.4.7/docs/install/iam_policy.json

# Create IAM policy
aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam_policy.json

# Create IAM service account
eksctl create iamserviceaccount \
  --cluster=crm-cluster \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --attach-policy-arn=arn:aws:iam::<AWS_ACCOUNT_ID>:policy/AWSLoadBalancerControllerIAMPolicy \
  --override-existing-serviceaccounts \
  --approve

# Install the controller using Helm
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=crm-cluster \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### Step 2: Build and Push Docker Image

#### 2.1 Using AWS ECR

**Linux/macOS:**
```bash
# Make script executable
chmod +x scripts/build-push.sh

# Run the script
./scripts/build-push.sh

# Follow the prompts:
# 1. Select "1" for AWS ECR
# 2. Enter AWS region (e.g., us-east-1)
# 3. Enter AWS account ID
# 4. Enter image tag (or press Enter for "latest")
```

**Windows:**
```cmd
# Run the script
scripts\build-push.bat

# Follow the prompts:
# 1. Select "1" for AWS ECR
# 2. Enter AWS region (e.g., us-east-1)
# 3. Enter AWS account ID
# 4. Enter image tag (or press Enter for "latest")
```

The script will:
- Sanitize the image name and tag
- Login to AWS ECR
- Create ECR repository if it doesn't exist
- Build the Docker image
- Push the image to ECR

**Example Output:**
```
Full image name: 123456789012.dkr.ecr.us-east-1.amazonaws.com/crm-container:latest
```

#### 2.2 Using Docker Hub (Alternative)

If you prefer Docker Hub:
```bash
# Run the script
./scripts/build-push.sh  # Linux/macOS
scripts\build-push.bat   # Windows

# Follow the prompts:
# 1. Select "2" for Docker Hub
# 2. Enter Docker Hub username
# 3. Enter Docker Hub password
# 4. Enter image tag
```

### Step 3: Deploy to AWS EKS

#### 3.1 Run Deployment Script

**Linux/macOS:**
```bash
# Make script executable
chmod +x scripts/deploy-image.sh

# Run the script
./scripts/deploy-image.sh
```

**Windows:**
```cmd
scripts\deploy-image.bat
```

#### 3.2 Follow the Prompts

The script will prompt for:

1. **AWS Region:** e.g., `us-east-1`
2. **EKS Cluster Name:** e.g., `crm-cluster`
3. **Docker Image URI:** Full image path from Step 2
   - Example: `123456789012.dkr.ecr.us-east-1.amazonaws.com/crm-container:latest`
4. **Database URL:** e.g., `jdbc:mysql://mysql-rds-endpoint:3306/crm?useSSL=false`
5. **Database Username:** e.g., `admin`
6. **Database Password:** (input will be hidden)

#### 3.3 Deployment Process

The script will:
1. Configure kubectl to connect to your EKS cluster
2. Verify cluster connectivity
3. Update Kubernetes manifests with your configuration
4. Create namespace: `crm-container`
5. Deploy the application (2 replicas)
6. Create ClusterIP service
7. Create ALB Ingress
8. Wait for deployment to complete
9. Display deployment information and access URL

**Example Output:**
```
==========================================
SUCCESS!
==========================================
Application deployed successfully to AWS EKS

Namespace: crm-container
Application: crm-container
Replicas: 2
Ingress URL: http://k8s-crmconta-crmconta-abc123-1234567890.us-east-1.elb.amazonaws.com
```

### Step 4: Verify Deployment

```bash
# Check all resources
kubectl get all -n crm-container

# Check pods
kubectl get pods -n crm-container

# Check service
kubectl get svc -n crm-container

# Check ingress
kubectl get ingress -n crm-container

# View pod logs
kubectl logs -n crm-container -l app=crm-container

# Describe pod for detailed information
kubectl describe pod -n crm-container -l app=crm-container
```

### Step 5: Access the Application

1. **Get the Load Balancer URL:**
   ```bash
   kubectl get ingress crm-container-ingress -n crm-container -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
   ```

2. **Wait for DNS propagation** (2-5 minutes)

3. **Access the application:**
   ```
   http://<load-balancer-url>
   ```

4. **Health check:**
   ```
   http://<load-balancer-url>/health
   ```

---

## Configuration Management

### Environment Variables

The application supports the following environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` | No |
| `SPRING_DATASOURCE_URL` | Database JDBC URL | - | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - | Yes |
| `MANAGEMENT_SECURITY_ENABLED` | Enable management security | `false` | No |
| `MANAGEMENT_CONTEXT_PATH` | Management endpoint path | `/appinfo` | No |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` | No |
| `TZ` | Timezone | `UTC` | No |

### Kubernetes ConfigMap (Optional)

For non-sensitive configuration:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: crm-config
  namespace: crm-container
data:
  application.properties: |
    spring.jpa.hibernate.ddl-auto=update
    spring.thymeleaf.mode=LEGACYHTML5
    spring.thymeleaf.cache=false
    management.security.enabled=false
```

### Kubernetes Secrets (Recommended for Sensitive Data)

```bash
# Create secret for database credentials
kubectl create secret generic crm-db-secret \
  --from-literal=username=admin \
  --from-literal=password=your-secure-password \
  -n crm-container

# Update deployment.yaml to use secrets:
# env:
# - name: SPRING_DATASOURCE_USERNAME
#   valueFrom:
#     secretKeyRef:
#       name: crm-db-secret
#       key: username
# - name: SPRING_DATASOURCE_PASSWORD
#   valueFrom:
#     secretKeyRef:
#       name: crm-db-secret
#       key: password
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Pod Fails to Start

**Symptoms:**
```bash
kubectl get pods -n crm-container
# NAME                             READY   STATUS             RESTARTS   AGE
# crm-container-xxx-yyy            0/1     CrashLoopBackOff   3          2m
```

**Diagnosis:**
```bash
# Check pod logs
kubectl logs -n crm-container -l app=crm-container

# Describe pod for events
kubectl describe pod -n crm-container -l app=crm-container
```

**Common Causes:**
- **Database connection failure:** Verify database URL, username, and password
- **Insufficient memory:** Increase memory limits in deployment.yaml
- **Missing environment variables:** Check all required env vars are set
- **Image pull errors:** Verify image URI and ECR permissions

**Solutions:**
```bash
# Update environment variables
kubectl edit deployment crm-container -n crm-container

# Restart deployment
kubectl rollout restart deployment/crm-container -n crm-container

# Scale down and up
kubectl scale deployment crm-container -n crm-container --replicas=0
kubectl scale deployment crm-container -n crm-container --replicas=2
```

#### 2. Ingress Not Working

**Symptoms:**
- Cannot access application via Load Balancer URL
- Ingress shows no address

**Diagnosis:**
```bash
# Check ingress status
kubectl describe ingress crm-container-ingress -n crm-container

# Check AWS Load Balancer Controller logs
kubectl logs -n kube-system -l app.kubernetes.io/name=aws-load-balancer-controller
```

**Solutions:**
- Verify AWS Load Balancer Controller is installed
- Check IAM permissions for Load Balancer Controller
- Verify security groups allow inbound traffic on port 80
- Wait 5-10 minutes for Load Balancer provisioning

#### 3. Health Check Failures

**Symptoms:**
```bash
# Pods keep restarting
# Readiness probe failed: HTTP probe failed
```

**Solutions:**
```bash
# Check if health endpoint is accessible
kubectl exec -it -n crm-container <pod-name> -- wget -O- http://localhost:8080/health

# Increase initialDelaySeconds in deployment.yaml
# Spring Boot apps may take 60-90 seconds to start
```

#### 4. Database Connection Issues

**Symptoms:**
- Application logs show "Connection refused" or "Unknown host"

**Solutions:**
- Verify database endpoint is accessible from EKS cluster
- Check security groups allow traffic from EKS nodes to database
- Verify database credentials are correct
- Test connection from a pod:
  ```bash
  kubectl run -it --rm debug --image=mysql:8 --restart=Never -n crm-container -- \
    mysql -h <db-host> -u <username> -p
  ```

#### 5. Out of Memory Errors

**Symptoms:**
```bash
# Pod logs show: java.lang.OutOfMemoryError
```

**Solutions:**
```yaml
# Update deployment.yaml resources:
resources:
  requests:
    memory: "768Mi"
  limits:
    memory: "1.5Gi"

# Update JAVA_OPTS:
env:
- name: JAVA_OPTS
  value: "-Xmx1024m -Xms512m -XX:MaxRAMPercentage=75.0"
```

### Useful Debugging Commands

```bash
# Get all resources in namespace
kubectl get all -n crm-container

# Watch pod status in real-time
kubectl get pods -n crm-container -w

# Get pod logs (last 100 lines)
kubectl logs -n crm-container -l app=crm-container --tail=100

# Follow logs in real-time
kubectl logs -n crm-container -l app=crm-container -f

# Execute command in pod
kubectl exec -it -n crm-container <pod-name> -- /bin/sh

# Port forward to local machine
kubectl port-forward -n crm-container svc/crm-container-service 8080:80

# Get events
kubectl get events -n crm-container --sort-by='.lastTimestamp'

# Check resource usage
kubectl top pods -n crm-container
kubectl top nodes
```

---

## Security Considerations

### 1. Image Security

- **Use specific image tags** instead of `latest` in production
- **Scan images for vulnerabilities:**
  ```bash
  # Using AWS ECR
  aws ecr start-image-scan --repository-name crm-container --image-id imageTag=v1.0.0
  
  # Using Trivy
  trivy image 123456789012.dkr.ecr.us-east-1.amazonaws.com/crm-container:latest
  ```

### 2. Secrets Management

- **Never commit secrets to Git**
- **Use Kubernetes Secrets or AWS Secrets Manager**
- **Rotate credentials regularly**

**Using AWS Secrets Manager:**
```bash
# Install External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets-system --create-namespace

# Create SecretStore and ExternalSecret
# See: https://external-secrets.io/latest/provider/aws-secrets-manager/
```

### 3. Network Security

- **Use Network Policies** to restrict pod-to-pod communication
- **Configure Security Groups** to allow only necessary traffic
- **Enable TLS/SSL** for production:
  ```yaml
  # Update ingress.yaml
  annotations:
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:region:account:certificate/xxx
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
  ```

### 4. RBAC (Role-Based Access Control)

```yaml
# Create service account with limited permissions
apiVersion: v1
kind: ServiceAccount
metadata:
  name: crm-app
  namespace: crm-container
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: crm-app-role
  namespace: crm-container
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list"]
```

### 5. Pod Security

- **Run as non-root user** (already configured in Dockerfile)
- **Set security context:**
  ```yaml
  securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    fsGroup: 1000
    capabilities:
      drop:
      - ALL
  ```

---

## Technology-Specific Notes

### Spring Boot 1.5.10 Considerations

1. **Actuator Endpoints:**
   - Spring Boot 1.5.x uses different actuator paths than 2.x
   - Health endpoint: `/health` (not `/actuator/health`)
   - Management context path: `/appinfo` (configured in application.properties)

2. **Java 8 Runtime:**
   - Using Amazon Corretto 8 as base image (as specified)
   - JVM options optimized for container environments
   - Heap size: 512MB max, 256MB initial

3. **Database Configuration:**
   - Application uses MySQL with JPA/Hibernate
   - DDL auto mode: `create-drop` (change to `update` or `validate` for production)
   - Connection pool managed by HikariCP (default in Spring Boot)

4. **Thymeleaf Configuration:**
   - Legacy HTML5 mode enabled
   - Template caching disabled for development
   - Enable caching in production for better performance

5. **Security:**
   - Spring Security 4.x (included with Spring Boot 1.5.x)
   - Management endpoints security disabled (for health checks)
   - Configure proper authentication for production

### Performance Tuning

1. **JVM Tuning:**
   ```yaml
   env:
   - name: JAVA_OPTS
     value: >-
       -Xmx512m
       -Xms256m
       -XX:+UseContainerSupport
       -XX:MaxRAMPercentage=75.0
       -XX:+UseG1GC
       -XX:MaxGCPauseMillis=200
       -XX:ParallelGCThreads=2
       -XX:ConcGCThreads=1
       -XX:InitiatingHeapOccupancyPercent=45
   ```

2. **Connection Pool:**
   ```properties
   # Add to application.properties
   spring.datasource.hikari.maximum-pool-size=10
   spring.datasource.hikari.minimum-idle=5
   spring.datasource.hikari.connection-timeout=30000
   spring.datasource.hikari.idle-timeout=600000
   spring.datasource.hikari.max-lifetime=1800000
   ```

3. **Horizontal Pod Autoscaling:**
   ```yaml
   apiVersion: autoscaling/v2
   kind: HorizontalPodAutoscaler
   metadata:
     name: crm-container-hpa
     namespace: crm-container
   spec:
     scaleTargetRef:
       apiVersion: apps/v1
       kind: Deployment
       name: crm-container
     minReplicas: 2
     maxReplicas: 10
     metrics:
     - type: Resource
       resource:
         name: cpu
         target:
           type: Utilization
           averageUtilization: 70
     - type: Resource
       resource:
         name: memory
         target:
           type: Utilization
           averageUtilization: 80
   ```

### Monitoring and Observability

1. **Spring Boot Actuator Endpoints:**
   - Health: `http://<app-url>/health`
   - Info: `http://<app-url>/appinfo/info`
   - Metrics: `http://<app-url>/appinfo/metrics`

2. **Prometheus Integration (Optional):**
   ```xml
   <!-- Add to pom.xml -->
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-prometheus</artifactId>
   </dependency>
   ```

3. **CloudWatch Container Insights:**
   ```bash
   # Install CloudWatch agent
   kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/quickstart/cwagent-fluentd-quickstart.yaml
   ```

---

## Scaling and High Availability

### Manual Scaling
```bash
# Scale to 5 replicas
kubectl scale deployment crm-container -n crm-container --replicas=5

# Verify scaling
kubectl get pods -n crm-container
```

### Auto-scaling
```bash
# Apply HPA (see Performance Tuning section)
kubectl apply -f hpa.yaml

# Check HPA status
kubectl get hpa -n crm-container
```

### Rolling Updates
```bash
# Update image
kubectl set image deployment/crm-container crm-container=<new-image-uri> -n crm-container

# Monitor rollout
kubectl rollout status deployment/crm-container -n crm-container

# Rollback if needed
kubectl rollout undo deployment/crm-container -n crm-container
```

---

## Cleanup

### Delete Kubernetes Resources
```bash
# Delete entire namespace (removes all resources)
kubectl delete namespace crm-container

# Or delete individual resources
kubectl delete -f kubernetes/
```

### Delete ECR Repository
```bash
aws ecr delete-repository --repository-name crm-container --region us-east-1 --force
```

### Delete EKS Cluster
```bash
# Using eksctl
eksctl delete cluster --name crm-cluster --region us-east-1

# This will take 10-15 minutes
```

---

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/1.5.x/reference/html/)
- [AWS EKS Documentation](https://docs.aws.amazon.com/eks/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [AWS Load Balancer Controller](https://kubernetes-sigs.github.io/aws-load-balancer-controller/)

---

## Support and Contribution

For issues, questions, or contributions, please contact the development team or create an issue in the project repository.

---

**Last Updated:** 2024
**Version:** 1.0.0
