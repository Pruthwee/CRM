# Cloud Deployment Guide for CRM Application

## Overview
This application has been modernized for cloud-native deployment on Google Cloud Platform (GCP). All cloud readiness blockers have been addressed.

## Cloud Readiness Fixes Applied

### 1. File System Dependencies Eliminated
- **ReadDataUtils.java**: Replaced local file system operations with Google Cloud Storage (GCS)
- **PdfController.java**: PDF generation now stores files in GCS instead of local filesystem
- **CSVTest.java**: CSV processing reads from GCS instead of local files

### 2. Configuration Management
- **application.properties**: All hardcoded values replaced with environment variables
- Database credentials, GCS bucket names, and other configs are externalized
- Connection pooling configured with HikariCP for cloud environments

### 3. Time/Clock Dependencies
- **DateTimeTestController.java**: All date/time operations use UTC timezone
- Eliminates timezone inconsistencies across distributed cloud deployments

### 4. Packaging
- Application uses JAR packaging (already configured in pom.xml)
- Dockerfile created for containerized deployment
- Multi-stage build optimizes image size

## Prerequisites

### GCP Setup
1. **GCP Project**: Create or use existing GCP project
2. **Enable APIs**:
   ```bash
   gcloud services enable container.googleapis.com
   gcloud services enable cloudbuild.googleapis.com
   gcloud services enable storage-api.googleapis.com
   gcloud services enable sqladmin.googleapis.com
   ```

3. **Create GCS Bucket**:
   ```bash
   gsutil mb -p PROJECT_ID -c STANDARD -l us-central1 gs://your-bucket-name
   ```

4. **Create Cloud SQL Instance** (MySQL):
   ```bash
   gcloud sql instances create crm-db \
     --database-version=MYSQL_8_0 \
     --tier=db-f1-micro \
     --region=us-central1
   ```

5. **Create Database**:
   ```bash
   gcloud sql databases create crm --instance=crm-db
   ```

6. **Create Service Account**:
   ```bash
   gcloud iam service-accounts create crm-sa \
     --display-name="CRM Application Service Account"
   
   # Grant necessary permissions
   gcloud projects add-iam-policy-binding PROJECT_ID \
     --member="serviceAccount:crm-sa@PROJECT_ID.iam.gserviceaccount.com" \
     --role="roles/storage.objectAdmin"
   
   gcloud projects add-iam-policy-binding PROJECT_ID \
     --member="serviceAccount:crm-sa@PROJECT_ID.iam.gserviceaccount.com" \
     --role="roles/cloudsql.client"
   ```

## Deployment Options

### Option 1: Google Kubernetes Engine (GKE)

#### 1. Build and Push Docker Image
```bash
# Build the image
docker build -t gcr.io/PROJECT_ID/crm:latest .

# Push to Google Container Registry
docker push gcr.io/PROJECT_ID/crm:latest
```

#### 2. Create GKE Cluster
```bash
gcloud container clusters create crm-cluster \
  --num-nodes=3 \
  --machine-type=n1-standard-2 \
  --region=us-central1 \
  --enable-autoscaling \
  --min-nodes=1 \
  --max-nodes=5 \
  --enable-workload-identity
```

#### 3. Configure Workload Identity
```bash
gcloud iam service-accounts add-iam-policy-binding \
  crm-sa@PROJECT_ID.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser \
  --member "serviceAccount:PROJECT_ID.svc.id.goog[default/crm-sa]"

kubectl annotate serviceaccount crm-sa \
  iam.gke.io/gcp-service-account=crm-sa@PROJECT_ID.iam.gserviceaccount.com
```

#### 4. Update k8s-deployment.yaml
Replace placeholders:
- `PROJECT_ID` with your GCP project ID
- `CLOUD_SQL_IP` with your Cloud SQL instance IP
- Update secrets with actual values

#### 5. Deploy to GKE
```bash
kubectl apply -f k8s-deployment.yaml
```

#### 6. Get External IP
```bash
kubectl get service crm-service
```

### Option 2: Cloud Run

#### 1. Build and Push Docker Image
```bash
gcloud builds submit --tag gcr.io/PROJECT_ID/crm:latest
```

#### 2. Update cloudrun-service.yaml
Replace placeholders:
- `PROJECT_ID` with your GCP project ID
- `REGION` with your region
- `INSTANCE_NAME` with your Cloud SQL instance name

#### 3. Deploy to Cloud Run
```bash
gcloud run services replace cloudrun-service.yaml --region=us-central1
```

Or use gcloud command directly:
```bash
gcloud run deploy crm-application \
  --image gcr.io/PROJECT_ID/crm:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --service-account crm-sa@PROJECT_ID.iam.gserviceaccount.com \
  --add-cloudsql-instances PROJECT_ID:us-central1:crm-db \
  --set-env-vars "DATABASE_URL=jdbc:mysql:///crm?cloudSqlInstance=PROJECT_ID:us-central1:crm-db&socketFactory=com.google.cloud.sql.mysql.SocketFactory" \
  --set-env-vars "DB_USERNAME=root" \
  --set-env-vars "DB_PASSWORD=your-password" \
  --set-env-vars "GCS_BUCKET_NAME=your-bucket-name" \
  --set-env-vars "GCP_PROJECT_ID=PROJECT_ID" \
  --memory 1Gi \
  --cpu 1 \
  --max-instances 10
```

### Option 3: App Engine Flexible

#### 1. Create app.yaml
```yaml
runtime: java
env: flex
runtime_config:
  jdk: openjdk8

automatic_scaling:
  min_num_instances: 1
  max_num_instances: 10
  cpu_utilization:
    target_utilization: 0.75

resources:
  cpu: 1
  memory_gb: 1
  disk_size_gb: 10

env_variables:
  GCS_BUCKET_NAME: "your-bucket-name"
  GCP_PROJECT_ID: "PROJECT_ID"
  DB_POOL_SIZE: "10"

beta_settings:
  cloud_sql_instances: "PROJECT_ID:us-central1:crm-db"
```

#### 2. Deploy
```bash
gcloud app deploy
```

## Environment Variables

### Required
- `DATABASE_URL`: JDBC connection string for Cloud SQL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `GCS_BUCKET_NAME`: Google Cloud Storage bucket name
- `GCP_PROJECT_ID`: GCP project ID

### Optional
- `PORT`: Application port (default: 8080)
- `DB_POOL_SIZE`: Connection pool size (default: 10)
- `DB_MIN_IDLE`: Minimum idle connections (default: 5)
- `GCS_PDF_FOLDER`: GCS folder for PDFs (default: pdfs)
- `GCS_CSV_FOLDER`: GCS folder for CSVs (default: csv)
- `LOG_LEVEL`: Root log level (default: INFO)
- `APP_LOG_LEVEL`: Application log level (default: DEBUG)

## Testing the Deployment

### Health Check
```bash
curl http://YOUR_SERVICE_URL/appinfo/health
```

### Test Endpoints
```bash
# Test date/time endpoint
curl http://YOUR_SERVICE_URL/date/test

# Test CSV export
curl http://YOUR_SERVICE_URL/customers

# Test PDF generator
curl http://YOUR_SERVICE_URL/pdf-generator
```

## Monitoring and Logging

### View Logs
```bash
# GKE
kubectl logs -l app=crm --tail=100 -f

# Cloud Run
gcloud run logs read crm-application --region=us-central1 --limit=100

# App Engine
gcloud app logs tail
```

### Metrics
Access Cloud Monitoring dashboard:
```bash
gcloud monitoring dashboards list
```

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
   - Verify Cloud SQL instance is running
   - Check service account has `cloudsql.client` role
   - Verify connection string format

2. **GCS Access Denied**
   - Verify service account has `storage.objectAdmin` role
   - Check bucket name is correct
   - Ensure bucket exists in the same project

3. **Application Won't Start**
   - Check logs for startup errors
   - Verify all required environment variables are set
   - Check resource limits (memory/CPU)

## Security Best Practices

1. **Never commit secrets**: Use Secret Manager or Kubernetes Secrets
2. **Use Workload Identity**: Avoid service account key files
3. **Enable VPC**: Use private IP for Cloud SQL
4. **Use HTTPS**: Enable SSL/TLS for all endpoints
5. **Regular Updates**: Keep dependencies updated

## Cost Optimization

1. **Use Autoscaling**: Configure appropriate min/max instances
2. **Right-size Resources**: Monitor and adjust CPU/memory limits
3. **Use Preemptible Nodes**: For non-critical workloads in GKE
4. **Enable Cloud CDN**: For static content
5. **Use Cloud Storage Lifecycle**: Archive old files

## Support

For issues or questions:
- Check application logs
- Review GCP documentation
- Contact cloud support team
