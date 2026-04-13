# CRM Component Pruthwee - Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Development Setup](#local-development-setup)
4. [Docker Deployment](#docker-deployment)
5. [GCP GKE Deployment](#gcp-gke-deployment)
6. [Configuration Management](#configuration-management)
7. [Troubleshooting](#troubleshooting)
8. [Security Considerations](#security-considerations)
9. [Technology-Specific Notes](#technology-specific-notes)

---

## Overview

This is a Spring Boot 1.5.10 application built with Java 8, featuring:
- **Framework**: Spring Boot with Spring MVC, Spring Data JPA, Spring Security
- **Build Tool**: Maven
- **Database**: MySQL (with H2 for development)
- **Template Engine**: Thymeleaf
- **Monitoring**: Spring Boot Actuator
- **Packaging**: JAR
- **Target Platform**: GCP GKE (Google Kubernetes Engine)

---

## Prerequisites

### Required Software
- **Java Development Kit (JDK) 8** or higher
- **Maven 3.6+** (for local builds)
- **Docker 20.10+** and Docker Compose 1.29+
- **Google Cloud SDK (gcloud)** - [Install Guide](https://cloud.google.com/sdk/docs/install)
- **kubectl** - Kubernetes command-line tool
- **Git** (for version control)

### GCP Requirements
- Active GCP account with billing enabled
- GCP project with GKE API enabled
- Appropriate IAM permissions:
  - Kubernetes Engine Admin
  - Service Account User
  - Artifact Registry Administrator (if using Artifact Registry)
- GKE cluster created and running

### External Services
- **MySQL Database**: Version 5.7 or 8.0
  - Database name: `crm`
  - Accessible from your deployment environment

---

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd CRM-component-pruthwee
```

### 2. Configure Application Properties
Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/crm?useSSL=false
spring.datasource.username=root
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update

# Actuator Configuration
management.security.enabled=false
management.context-path=/actuator
endpoints.health.enabled=true
```

### 3. Build the Application
```bash
mvn clean package -DskipTests
```

### 4. Run Locally
```bash
java -jar target/crm-0.0.1-SNAPSHOT.jar
```

Access the application at: `http://localhost:8080`

Health check endpoint: `http://localhost:8080/actuator/health`

---

## Docker Deployment

### 1. Build Docker Image Locally
```bash
docker build -t crm-component-pruthwee:latest .
```

### 2. Run with Docker Compose

**Important**: The `docker-compose.yml` contains only the application service. You must provide external MySQL database connection details.

Edit `docker-compose.yml` to configure your database connection:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/crm?useSSL=false
  - SPRING_DATASOURCE_USERNAME=your_username
  - SPRING_DATASOURCE_PASSWORD=your_password
```

Start the application:
```bash
docker-compose up -d
```

View logs:
```bash
docker-compose logs -f
```

Stop the application:
```bash
docker-compose down
```

### 3. Build and Push to Registry

#### Option A: Google Artifact Registry

**Linux/macOS:**
```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

**Windows:**
```cmd
scripts\build-push.bat
```

Follow the prompts:
1. Select "1" for Google Artifact Registry
2. Enter your GCP Project ID
3. Enter GCP Region (e.g., `us-central1`)
4. Enter Artifact Registry repository name
5. Enter image tag (default: `latest`)

#### Option B: Docker Hub

Follow the same script prompts but select "2" for Docker Hub and provide your Docker Hub credentials.

---

## GCP GKE Deployment

### Prerequisites Setup

#### 1. Install and Configure gcloud CLI
```bash
# Install gcloud (if not already installed)
# Follow: https://cloud.google.com/sdk/docs/install

# Initialize gcloud
gcloud init

# Set your project
gcloud config set project YOUR_PROJECT_ID

# Authenticate
gcloud auth login
```

#### 2. Create GKE Cluster (if not exists)
```bash
# Create a GKE cluster
gcloud container clusters create crm-cluster \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type n1-standard-2 \
  --enable-autoscaling \
  --min-nodes 2 \
  --max-nodes 5

# Get cluster credentials
gcloud container clusters get-credentials crm-cluster \
  --zone us-central1-a
```

#### 3. Verify kubectl Configuration
```bash
kubectl cluster-info
kubectl get nodes
```

### Deployment Steps

#### 1. Build and Push Docker Image
Use the build-push scripts as described in the Docker Deployment section to push your image to Google Artifact Registry or Docker Hub.

Example image URI:
- Artifact Registry: `us-central1-docker.pkg.dev/my-project/my-repo/crm-component-pruthwee:latest`
- Docker Hub: `myusername/crm-component-pruthwee:latest`

#### 2. Deploy to GKE

**Linux/macOS:**
```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

**Windows:**
```cmd
scripts\deploy-image.bat
```

Follow the prompts:
1. Enter GCP Project ID
2. Enter GCP Zone (e.g., `us-central1-a`)
3. Enter GKE Cluster Name
4. Enter Docker Image URI (from step 1)
5. Configure environment variables:
   - `SPRING_DATASOURCE_URL`: Your MySQL connection string
   - `SPRING_DATASOURCE_USERNAME`: Database username
   - `SPRING_DATASOURCE_PASSWORD`: Database password

#### 3. Verify Deployment
```bash
# Check namespace
kubectl get namespace crm-component-pruthwee

# Check pods
kubectl get pods -n crm-component-pruthwee

# Check services
kubectl get svc -n crm-component-pruthwee

# Check ingress
kubectl get ingress -n crm-component-pruthwee

# View logs
kubectl logs -f deployment/crm-component-pruthwee -n crm-component-pruthwee
```

#### 4. Access the Application

**Port Forwarding (for testing):**
```bash
kubectl port-forward -n crm-component-pruthwee svc/crm-component-pruthwee-service 8080:80
```
Then access: `http://localhost:8080`

**Via Ingress (production):**
```bash
# Get ingress IP address
kubectl get ingress -n crm-component-pruthwee

# Wait for IP to be assigned (may take 5-10 minutes)
# Access via: http://<INGRESS_IP>
```

### Kubernetes Resource Management

#### Scaling
```bash
# Scale deployment
kubectl scale deployment crm-component-pruthwee -n crm-component-pruthwee --replicas=3

# Enable Horizontal Pod Autoscaler
kubectl autoscale deployment crm-component-pruthwee \
  -n crm-component-pruthwee \
  --cpu-percent=70 \
  --min=2 \
  --max=10
```

#### Rolling Updates
```bash
# Update image
kubectl set image deployment/crm-component-pruthwee \
  crm-component-pruthwee=NEW_IMAGE_URI \
  -n crm-component-pruthwee

# Check rollout status
kubectl rollout status deployment/crm-component-pruthwee -n crm-component-pruthwee

# View rollout history
kubectl rollout history deployment/crm-component-pruthwee -n crm-component-pruthwee
```

#### Rollback
```bash
# Rollback to previous version
kubectl rollout undo deployment/crm-component-pruthwee -n crm-component-pruthwee

# Rollback to specific revision
kubectl rollout undo deployment/crm-component-pruthwee -n crm-component-pruthwee --to-revision=2
```

---

## Configuration Management

### Environment Variables

The application supports the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | MySQL connection URL | `jdbc:mysql://localhost:3306/crm` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL mode | `update` |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` |
| `TZ` | Timezone | `UTC` |

### Kubernetes Secrets (Recommended for Production)

Create a secret for sensitive data:
```bash
kubectl create secret generic crm-db-secret \
  -n crm-component-pruthwee \
  --from-literal=username=your_username \
  --from-literal=password=your_password
```

Update `kubernetes/deployment.yaml` to use secrets:
```yaml
env:
- name: SPRING_DATASOURCE_USERNAME
  valueFrom:
    secretKeyRef:
      name: crm-db-secret
      key: username
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: crm-db-secret
      key: password
```

### ConfigMaps

Create a ConfigMap for non-sensitive configuration:
```bash
kubectl create configmap crm-config \
  -n crm-component-pruthwee \
  --from-literal=database.url=jdbc:mysql://mysql-host:3306/crm
```

---

## Troubleshooting

### Common Issues

#### 1. Pod Not Starting
```bash
# Check pod status
kubectl describe pod <pod-name> -n crm-component-pruthwee

# Check logs
kubectl logs <pod-name> -n crm-component-pruthwee

# Common causes:
# - Image pull errors (check image URI and registry authentication)
# - Database connection failures (verify SPRING_DATASOURCE_URL)
# - Insufficient resources (check resource limits)
```

#### 2. Database Connection Issues
```bash
# Test database connectivity from pod
kubectl exec -it <pod-name> -n crm-component-pruthwee -- /bin/sh
# Inside pod:
# curl -v telnet://mysql-host:3306

# Verify environment variables
kubectl exec <pod-name> -n crm-component-pruthwee -- env | grep SPRING_DATASOURCE
```

#### 3. Health Check Failures
```bash
# Check health endpoint
kubectl port-forward <pod-name> -n crm-component-pruthwee 8080:8080
curl http://localhost:8080/actuator/health

# Adjust probe timings in deployment.yaml if needed:
# - Increase initialDelaySeconds for slow startup
# - Increase timeoutSeconds for slow responses
```

#### 4. Ingress Not Working
```bash
# Check ingress status
kubectl describe ingress crm-component-pruthwee-ingress -n crm-component-pruthwee

# Verify ingress controller is running
kubectl get pods -n kube-system | grep ingress

# Check GCP Load Balancer
gcloud compute forwarding-rules list
gcloud compute backend-services list
```

#### 5. Image Pull Errors
```bash
# For Artifact Registry
gcloud auth configure-docker us-central1-docker.pkg.dev

# Create image pull secret
kubectl create secret docker-registry gcr-secret \
  -n crm-component-pruthwee \
  --docker-server=us-central1-docker.pkg.dev \
  --docker-username=_json_key \
  --docker-password="$(cat key.json)"

# Add to deployment.yaml:
# spec:
#   imagePullSecrets:
#   - name: gcr-secret
```

### Debugging Commands

```bash
# Get all resources in namespace
kubectl get all -n crm-component-pruthwee

# Describe deployment
kubectl describe deployment crm-component-pruthwee -n crm-component-pruthwee

# Get events
kubectl get events -n crm-component-pruthwee --sort-by='.lastTimestamp'

# Execute commands in pod
kubectl exec -it <pod-name> -n crm-component-pruthwee -- /bin/sh

# View resource usage
kubectl top pods -n crm-component-pruthwee
kubectl top nodes
```

---

## Security Considerations

### 1. Use Secrets for Sensitive Data
- Never hardcode passwords in deployment files
- Use Kubernetes Secrets or GCP Secret Manager
- Rotate credentials regularly

### 2. Network Policies
Create network policies to restrict pod communication:
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: crm-network-policy
  namespace: crm-component-pruthwee
spec:
  podSelector:
    matchLabels:
      app: crm-component-pruthwee
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
```

### 3. RBAC Configuration
Implement Role-Based Access Control:
```bash
# Create service account
kubectl create serviceaccount crm-sa -n crm-component-pruthwee

# Create role and role binding
kubectl create role crm-role --verb=get,list --resource=pods -n crm-component-pruthwee
kubectl create rolebinding crm-binding --role=crm-role --serviceaccount=crm-component-pruthwee:crm-sa -n crm-component-pruthwee
```

### 4. Image Security
- Use official base images (eclipse-temurin)
- Scan images for vulnerabilities
- Run containers as non-root user (already configured)
- Keep base images updated

### 5. TLS/SSL Configuration
For production, configure HTTPS:
```yaml
# In ingress.yaml
metadata:
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - crm-component-pruthwee.example.com
    secretName: crm-tls-secret
```

---

## Technology-Specific Notes

### Spring Boot 1.5.10 Considerations

#### 1. Actuator Endpoints
Spring Boot 1.5.x uses different actuator paths:
- Health: `/actuator/health` (configured via `management.context-path`)
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`

#### 2. JVM Memory Configuration
The application is configured with:
```
-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

Adjust based on your workload:
- For higher traffic: Increase to `-Xmx1g -Xms512m`
- For lower traffic: Decrease to `-Xmx256m -Xms128m`

#### 3. Database Connection Pooling
Spring Boot 1.5.x uses Tomcat JDBC pool by default. Configure in `application.properties`:
```properties
spring.datasource.tomcat.max-active=20
spring.datasource.tomcat.max-idle=10
spring.datasource.tomcat.min-idle=5
```

#### 4. Thymeleaf Configuration
The application uses LEGACYHTML5 mode:
```properties
spring.thymeleaf.mode=LEGACYHTML5
spring.thymeleaf.cache=false
```

For production, enable caching:
```properties
spring.thymeleaf.cache=true
```

#### 5. Security Configuration
Spring Security 4.x is used. Ensure proper configuration in `SecurityConfig.java` for:
- Authentication providers
- Password encoding
- CSRF protection
- Session management

### Maven Build Optimization

#### Dependency Caching
The Dockerfile is optimized for dependency caching:
1. Copy `pom.xml` first
2. Download dependencies
3. Copy source code
4. Build application

This ensures dependencies are only re-downloaded when `pom.xml` changes.

#### Multi-Module Projects
If your project has multiple modules, adjust the Dockerfile:
```dockerfile
# Copy parent POM
COPY pom.xml .
# Copy module POMs
COPY module1/pom.xml module1/
COPY module2/pom.xml module2/
# Download dependencies
RUN mvn dependency:go-offline -B
# Copy source
COPY module1/src module1/src
COPY module2/src module2/src
# Build
RUN mvn clean package -DskipTests -B
```

### Performance Tuning

#### 1. JVM Tuning
```bash
# For better GC performance
JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# For better startup time
JAVA_OPTS="-XX:TieredStopAtLevel=1"

# For debugging
JAVA_OPTS="-XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
```

#### 2. Spring Boot Tuning
```properties
# Connection pool
spring.datasource.tomcat.max-active=50
spring.datasource.tomcat.max-wait=10000

# Thread pool
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10

# Compression
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,application/json
```

#### 3. Kubernetes Resource Limits
Adjust based on load testing:
```yaml
resources:
  requests:
    cpu: "500m"
    memory: "768Mi"
  limits:
    cpu: "1000m"
    memory: "1.5Gi"
```

---

## Monitoring and Observability

### 1. Spring Boot Actuator
Access actuator endpoints:
```bash
# Health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

### 2. Kubernetes Monitoring
```bash
# Pod metrics
kubectl top pods -n crm-component-pruthwee

# Node metrics
kubectl top nodes

# Resource usage
kubectl describe node <node-name>
```

### 3. GCP Monitoring
Enable GCP monitoring:
```bash
# Enable Cloud Monitoring
gcloud services enable monitoring.googleapis.com

# View logs in Cloud Logging
gcloud logging read "resource.type=k8s_container AND resource.labels.namespace_name=crm-component-pruthwee" --limit 50
```

### 4. Application Logs
```bash
# Stream logs
kubectl logs -f deployment/crm-component-pruthwee -n crm-component-pruthwee

# Logs from all pods
kubectl logs -f -l app=crm-component-pruthwee -n crm-component-pruthwee

# Previous container logs
kubectl logs <pod-name> -n crm-component-pruthwee --previous
```

---

## Backup and Disaster Recovery

### 1. Database Backups
Ensure regular MySQL backups:
```bash
# Manual backup
kubectl exec -it <mysql-pod> -- mysqldump -u root -p crm > backup.sql

# Automated backups (configure in your MySQL deployment)
```

### 2. Kubernetes Resource Backups
```bash
# Export all resources
kubectl get all -n crm-component-pruthwee -o yaml > backup.yaml

# Backup specific resources
kubectl get deployment,service,ingress -n crm-component-pruthwee -o yaml > resources-backup.yaml
```

### 3. Disaster Recovery Plan
1. Maintain infrastructure as code (Kubernetes manifests)
2. Store Docker images in multiple registries
3. Document recovery procedures
4. Test recovery process regularly
5. Maintain database backups with point-in-time recovery

---

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/1.5.x/reference/html/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [GKE Documentation](https://cloud.google.com/kubernetes-engine/docs)
- [Docker Documentation](https://docs.docker.com/)
- [Maven Documentation](https://maven.apache.org/guides/)

---

## Support and Maintenance

For issues or questions:
1. Check application logs: `kubectl logs -f deployment/crm-component-pruthwee -n crm-component-pruthwee`
2. Review Kubernetes events: `kubectl get events -n crm-component-pruthwee`
3. Verify configuration: `kubectl describe deployment crm-component-pruthwee -n crm-component-pruthwee`
4. Contact your DevOps team or system administrator

---

**Last Updated**: 2024
**Version**: 1.0.0
