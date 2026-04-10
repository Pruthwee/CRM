# CRM Application - Cloud-Ready for GCP

This application has been modernized for cloud deployment on Google Cloud Platform (GCP).

## Cloud Readiness Improvements

### 1. File System Dependencies Eliminated
- **Before**: Used local file system with `java.io.File` and hardcoded paths
- **After**: Integrated Google Cloud Storage (GCS) for all file operations
- **Files Modified**: 
  - `ReadDataUtils.java` - Now uses GCS for file reading
  - `PdfController.java` - Stores PDFs in GCS instead of local filesystem
  - `CSVTest.java` - Reads CSV files from GCS

### 2. Asynchronous I/O Operations
- **Before**: Synchronous blocking I/O operations
- **After**: Implemented `CompletableFuture` for non-blocking async operations
- **Benefits**: Better throughput and resource utilization in cloud environments

### 3. Time Zone Standardization
- **Before**: Used local server time with `Date()` and `LocalDateTime.now()`
- **After**: Standardized on UTC timestamps using `Instant.now(Clock.systemUTC())`
- **Files Modified**: `DateTimeTestController.java`

### 4. Configuration Management
- **Before**: Hardcoded database credentials and configuration
- **After**: Environment variable-based configuration
- **Files Modified**: `application.properties`, `application-gcp.properties`

### 5. Connection Pooling
- **Added**: HikariCP connection pool configuration for Cloud SQL
- **Benefits**: Efficient database connection management in cloud

## Environment Variables

Required environment variables for cloud deployment:

```bash
# Database Configuration
DATABASE_URL=jdbc:mysql:///crm?cloudSqlInstance=PROJECT:REGION:INSTANCE&socketFactory=com.google.cloud.sql.mysql.SocketFactory
DATABASE_USERNAME=root
DATABASE_PASSWORD=your-password

# GCS Configuration
GCS_BUCKET_NAME=crm-data-bucket
GCP_PROJECT_ID=your-project-id

# Application Configuration
SPRING_PROFILES_ACTIVE=gcp
PORT=8080
```

## Deployment Options

### Option 1: Google Cloud Run

```bash
# Build and deploy
gcloud builds submit --config=cloudbuild.yaml

# Or manually
docker build -t gcr.io/PROJECT_ID/crm-app .
docker push gcr.io/PROJECT_ID/crm-app
gcloud run deploy crm-app --image gcr.io/PROJECT_ID/crm-app --platform managed
```

### Option 2: Google App Engine

```bash
# Deploy to App Engine
gcloud app deploy app.yaml
```

### Option 3: Google Kubernetes Engine (GKE)

```bash
# Build image
docker build -t gcr.io/PROJECT_ID/crm-app .
docker push gcr.io/PROJECT_ID/crm-app

# Deploy to GKE
kubectl apply -f k8s/deployment.yaml
```

## Pre-Deployment Setup

### 1. Create GCS Bucket

```bash
gsutil mb -p PROJECT_ID -c STANDARD -l us-central1 gs://crm-data-bucket/
```

### 2. Set up Cloud SQL

```bash
gcloud sql instances create crm-db \
  --database-version=MYSQL_8_0 \
  --tier=db-f1-micro \
  --region=us-central1

gcloud sql databases create crm --instance=crm-db
```

### 3. Configure IAM Permissions

```bash
# Grant Cloud SQL Client role
gcloud projects add-iam-policy-binding PROJECT_ID \
  --member=serviceAccount:SERVICE_ACCOUNT \
  --role=roles/cloudsql.client

# Grant Storage Object Admin role
gcloud projects add-iam-policy-binding PROJECT_ID \
  --member=serviceAccount:SERVICE_ACCOUNT \
  --role=roles/storage.objectAdmin
```

## Local Development

For local development with GCS emulator:

```bash
# Start GCS emulator
gcloud beta emulators storage start

# Set environment variable
export STORAGE_EMULATOR_HOST=http://localhost:9023

# Run application
mvn spring-boot:run
```

## Health Checks

The application exposes health check endpoints:

- **Health**: `/appinfo/health`
- **Info**: `/appinfo/info`
- **Metrics**: `/appinfo/metrics`

## Monitoring and Logging

- All logs are written to stdout/stderr for Cloud Logging integration
- Structured logging format for better log analysis
- UTC timestamps for consistent time tracking across regions

## Architecture Changes

### Before (Non-Cloud-Ready)
```
Application → Local File System
Application → Direct JDBC Connection
Application → Local Time Zone
```

### After (Cloud-Ready)
```
Application → Google Cloud Storage (GCS)
Application → Cloud SQL with Connection Pooling
Application → UTC Time Zone
Application → Async I/O Operations
```

## Dependencies Added

- `google-cloud-storage:2.22.3` - GCS client library
- HikariCP (via Spring Boot) - Connection pooling
- SLF4J - Structured logging

## Testing

```bash
# Run tests
mvn test

# Run with GCP profile
mvn spring-boot:run -Dspring-boot.run.profiles=gcp
```

## Troubleshooting

### Issue: Cannot connect to Cloud SQL
**Solution**: Ensure Cloud SQL Proxy is configured or using socket factory

### Issue: GCS authentication fails
**Solution**: Set `GOOGLE_APPLICATION_CREDENTIALS` environment variable

### Issue: Application timeout
**Solution**: Increase timeout in `app.yaml` or Cloud Run configuration

## Support

For issues or questions, refer to:
- [Google Cloud Documentation](https://cloud.google.com/docs)
- [Spring Boot on GCP](https://spring.io/guides/gs/spring-boot-gcp/)
