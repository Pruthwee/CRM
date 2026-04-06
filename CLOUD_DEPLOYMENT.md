# Cloud Deployment Guide - GCP

This document describes the cloud-ready transformations applied to the CRM application and how to deploy it to Google Cloud Platform (GCP).

## Cloud Readiness Transformations Applied

### 1. Configuration Management
- ✅ Externalized all hardcoded configuration values to environment variables
- ✅ Added `application-gcp.properties` for GCP-specific configuration
- ✅ Configured HikariCP connection pooling for database connections
- ✅ Added support for GCP Cloud SQL with socket factory

### 2. File System Dependencies
- ✅ Replaced local file system writes with GCP Cloud Storage
- ✅ Created `CloudStorageService` for cloud-native file operations
- ✅ Updated `PdfController` to use Cloud Storage instead of local files
- ✅ Removed Swing file chooser dependencies from `ReadDataUtils`
- ✅ Replaced `java.io.File` usage with classpath resources and Cloud Storage

### 3. Static Initializers
- ✅ Removed static initializer with I/O operations from `CSVTest`
- ✅ Converted to Spring component with proper lifecycle management
- ✅ Added `@PostConstruct` initialization in `CloudStorageService`

### 4. Timezone Management
- ✅ Standardized on UTC timezone for all internal operations
- ✅ Updated `DateTimeTestController` to use UTC with configurable display timezone
- ✅ Configured Hibernate to use UTC timezone
- ✅ Updated `PdfView` to use UTC for timestamp generation

### 5. Packaging & Deployment
- ✅ Application already packaged as executable JAR (not WAR)
- ✅ Created `Dockerfile` for containerized deployment
- ✅ Added `.dockerignore` for optimized Docker builds
- ✅ Created `cloudbuild.yaml` for GCP Cloud Build CI/CD
- ✅ Created `k8s-deployment.yaml` for GKE deployment

### 6. Logging & Monitoring
- ✅ Added structured logging with SLF4J
- ✅ Configured JSON logging pattern for GCP Cloud Logging
- ✅ Added health check endpoints for container orchestration

## Environment Variables

The following environment variables must be configured for cloud deployment:

### Database Configuration
```bash
DATABASE_URL=jdbc:mysql://CLOUD_SQL_HOST:3306/crm?useSSL=false
DATABASE_USERNAME=your-db-username
DATABASE_PASSWORD=your-db-password
DB_POOL_SIZE=10
DB_CONNECTION_TIMEOUT=30000
```

### GCP Cloud Storage
```bash
GCP_STORAGE_BUCKET=your-bucket-name
GCP_PROJECT_ID=your-gcp-project-id
GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

### Application Configuration
```bash
SPRING_PROFILES_ACTIVE=gcp
PORT=8080
```

## Deployment Options

### Option 1: GCP Cloud Run (Recommended for Serverless)

1. Build and push Docker image:
```bash
gcloud builds submit --tag gcr.io/PROJECT_ID/crm-app
```

2. Deploy to Cloud Run:
```bash
gcloud run deploy crm-app \
  --image gcr.io/PROJECT_ID/crm-app \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars SPRING_PROFILES_ACTIVE=gcp \
  --set-env-vars DATABASE_URL=jdbc:mysql://... \
  --set-env-vars GCP_STORAGE_BUCKET=your-bucket
```

### Option 2: GCP GKE (Kubernetes)

1. Create GKE cluster:
```bash
gcloud container clusters create crm-cluster \
  --num-nodes=3 \
  --machine-type=n1-standard-2 \
  --region=us-central1
```

2. Build and push Docker image:
```bash
docker build -t gcr.io/PROJECT_ID/crm-app .
docker push gcr.io/PROJECT_ID/crm-app
```

3. Update `k8s-deployment.yaml` with your project ID and secrets

4. Deploy to GKE:
```bash
kubectl apply -f k8s-deployment.yaml
```

### Option 3: GCP Compute Engine

1. Create VM instance with Container-Optimized OS
2. Deploy using Docker:
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=gcp \
  -e DATABASE_URL=... \
  -e GCP_STORAGE_BUCKET=... \
  gcr.io/PROJECT_ID/crm-app
```

## GCP Services Required

### 1. Cloud SQL (MySQL)
- Create Cloud SQL instance
- Configure private IP or Cloud SQL Proxy
- Create database: `crm`
- Set up database user and password

### 2. Cloud Storage
- Create storage bucket for file uploads
- Configure bucket permissions
- Enable versioning (optional)

### 3. Secret Manager (Recommended)
- Store database credentials
- Store API keys and sensitive configuration

### 4. Cloud Build (for CI/CD)
- Enable Cloud Build API
- Configure build triggers from Git repository

## Health Checks

The application exposes health check endpoints:
- `/appinfo/health` - Application health status
- `/appinfo/info` - Application information
- `/appinfo/metrics` - Application metrics

## Monitoring

Configure GCP Cloud Monitoring:
1. Enable Cloud Monitoring API
2. Application logs automatically sent to Cloud Logging
3. Set up alerts for error rates and latency
4. Configure uptime checks for health endpoints

## Security Considerations

1. **Database**: Use Cloud SQL with private IP and IAM authentication
2. **Storage**: Configure bucket IAM policies with least privilege
3. **Secrets**: Use Secret Manager instead of environment variables for production
4. **Network**: Deploy in VPC with firewall rules
5. **Container**: Application runs as non-root user

## Cost Optimization

1. **Cloud Run**: Auto-scales to zero when not in use
2. **GKE**: Use preemptible nodes for non-production
3. **Cloud SQL**: Use appropriate machine type and enable automatic backups
4. **Cloud Storage**: Use lifecycle policies for old files

## Troubleshooting

### Application won't start
- Check environment variables are set correctly
- Verify database connectivity
- Check Cloud Storage bucket permissions
- Review logs in Cloud Logging

### Database connection issues
- Verify Cloud SQL instance is running
- Check firewall rules
- Verify credentials in Secret Manager
- Test connection from Cloud Shell

### File upload failures
- Verify Cloud Storage bucket exists
- Check IAM permissions for service account
- Verify bucket name in configuration

## Next Steps

1. Set up CI/CD pipeline with Cloud Build
2. Configure Cloud CDN for static assets
3. Implement Cloud Armor for DDoS protection
4. Set up Cloud Monitoring dashboards
5. Configure automated backups
6. Implement disaster recovery plan

## Support

For issues or questions:
- Check GCP documentation: https://cloud.google.com/docs
- Review application logs in Cloud Logging
- Contact cloud operations team
