# Cloud Readiness Transformation - CRM Application

## Overview
This document describes the cloud readiness transformations applied to make the CRM application fully compatible with Google Cloud Platform (GCP) deployment.

## Transformations Applied

### 1. File System Dependencies → Google Cloud Storage (GCS)

#### Issue: Hard-coded File Paths (cr-java-0061)
**File:** `crm/utils/ReadDataUtils.java`
- **Problem:** Used JFileChooser (desktop GUI) for file selection with hard-coded paths
- **Solution:** Replaced with GCS-based file reading using Cloud Storage Java SDK
- **Changes:**
  - Removed javax.swing dependencies
  - Added GCS blob reading with configurable bucket names
  - Added classpath resource reading for bundled files
  - Implemented proper error handling and logging

#### Issue: Local File System Write Operations (cr-java-0062)
**File:** `crm/controller/PdfController.java`
- **Problem:** Wrote PDF files directly to local filesystem using FileOutputStream
- **Solution:** Generate PDFs in memory and upload to GCS
- **Changes:**
  - Generate PDF to ByteArrayOutputStream instead of FileOutputStream
  - Upload PDF content to GCS with proper metadata
  - Store GCS URI (gs://bucket/path) instead of local file path
  - Added timestamp-based naming to prevent conflicts

#### Issue: Java.io.File Usage for Data Storage (cr-java-0063)
**File:** `crm/csv/CSVTest.java`
- **Problem:** Used java.io.File for CSV file operations
- **Solution:** Read CSV files from GCS or classpath resources
- **Changes:**
  - Replaced File-based reading with GCS blob reading
  - Process CSV from byte arrays instead of file handles
  - Added support for classpath resources for bundled test data

### 2. Static Initializers → Lazy Initialization (cr-java-0105)

#### Issue: Static Initializers with I/O
**File:** `crm/csv/CSVTest.java`
- **Problem:** Static main method with I/O operations during class loading
- **Solution:** Converted to Spring component with @PostConstruct initialization
- **Changes:**
  - Removed static main method
  - Added @Component annotation for Spring management
  - Used @PostConstruct for lazy initialization
  - Implemented proper dependency injection

### 3. Clock/Time Dependencies → UTC Standardization (cr-java-0111)

#### Issue: Local Timezone Dependencies
**File:** `crm/controller/DateTimeTestController.java`
- **Problem:** Used java.util.Date and LocalDateTime.now() without timezone awareness
- **Solution:** Standardized on UTC timestamps for all time operations
- **Changes:**
  - Replaced Date with Instant (UTC-based)
  - Use ZonedDateTime with explicit UTC timezone
  - Use LocalDate.now(ZoneOffset.UTC) for date operations
  - Added ISO-8601 formatted timestamps for interoperability

### 4. WAR Packaging → Executable JAR (cr-java-0107)

#### Issue: WAR Packaging Dependencies
**Files:** Multiple view classes using javax.servlet
- **Problem:** Code assumed WAR deployment to external application server
- **Solution:** Already configured as JAR in pom.xml, servlet dependencies compatible
- **Status:** No changes needed - pom.xml already has `<packaging>jar</packaging>`

### 5. Configuration Management → Environment Variables

#### Issue: Hard-coded Configuration Values
**File:** `src/main/resources/application.properties`
- **Problem:** Hard-coded database credentials, URLs, and configuration
- **Solution:** Externalized all configuration to environment variables
- **Changes:**
  - Database URL: `${DATABASE_URL:jdbc:mysql://localhost:3306/crm}`
  - Database credentials: `${DB_USERNAME:root}`, `${DB_PASSWORD:password}`
  - GCS configuration: `${GCS_BUCKET_NAME}`, `${GCP_PROJECT_ID}`
  - Server port: `${PORT:8080}`
  - Added HikariCP connection pool configuration
  - Added graceful shutdown for container orchestration

## New Dependencies Added

### Google Cloud Storage
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-storage</artifactId>
    <version>2.17.2</version>
</dependency>
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-core</artifactId>
    <version>2.9.4</version>
</dependency>
```

## Environment Variables Required

### Required for Production
- `GCS_BUCKET_NAME` - Google Cloud Storage bucket name for file storage
- `DATABASE_URL` - Cloud SQL connection string
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

### Optional Configuration
- `GCP_PROJECT_ID` - GCP project ID (uses ADC if not set)
- `GCS_PDF_FOLDER` - Folder path for PDFs in GCS (default: "pdfs")
- `GCS_CSV_FOLDER` - Folder path for CSV files in GCS (default: "csv-files")
- `PORT` - Server port (default: 8080)
- `DB_POOL_SIZE` - Database connection pool size (default: 10)
- `LOG_LEVEL` - Root logging level (default: INFO)

## Deployment Instructions

### Google Kubernetes Engine (GKE)
1. Build Docker image:
   ```bash
   docker build -t gcr.io/[PROJECT_ID]/crm-app:latest .
   ```

2. Push to Container Registry:
   ```bash
   docker push gcr.io/[PROJECT_ID]/crm-app:latest
   ```

3. Deploy to GKE:
   ```bash
   kubectl apply -f k8s/deployment.yaml
   ```

### Cloud Run
1. Deploy directly from source:
   ```bash
   gcloud run deploy crm-app \
     --source . \
     --platform managed \
     --region us-central1 \
     --set-env-vars GCS_BUCKET_NAME=your-bucket,DATABASE_URL=your-db-url
   ```

### App Engine Flexible
1. Create app.yaml:
   ```yaml
   runtime: java
   env: flex
   runtime_config:
     jdk: openjdk8
   env_variables:
     GCS_BUCKET_NAME: "your-bucket"
     DATABASE_URL: "your-db-url"
   ```

2. Deploy:
   ```bash
   gcloud app deploy
   ```

## Authentication

The application uses **Application Default Credentials (ADC)** for GCP authentication:
- In GKE/Cloud Run: Automatically uses Workload Identity
- In App Engine: Automatically uses App Engine service account
- Local development: Use `gcloud auth application-default login`

## Database Configuration

### Cloud SQL Connection
For Cloud SQL, use the connection string format:
```
jdbc:mysql://google/[DATABASE]?cloudSqlInstance=[INSTANCE_CONNECTION_NAME]&socketFactory=com.google.cloud.sql.mysql.SocketFactory
```

Or use Cloud SQL Proxy for local development.

## Monitoring and Logging

- All logs use structured format compatible with Cloud Logging
- Actuator endpoints available at `/appinfo`
- Health checks available at `/appinfo/health`
- Metrics available at `/appinfo/metrics`

## Cloud-Native Features Implemented

✅ Stateless application design
✅ Externalized configuration via environment variables
✅ Cloud storage integration (GCS)
✅ Connection pooling with HikariCP
✅ Graceful shutdown support
✅ UTC-based time handling
✅ Structured logging
✅ Health check endpoints
✅ Container-ready packaging (executable JAR)
✅ 12-factor app compliance

## Testing

### Local Testing with GCS Emulator
```bash
# Start GCS emulator
gcloud beta emulators storage start

# Set environment variable
export STORAGE_EMULATOR_HOST=http://localhost:9023

# Run application
mvn spring-boot:run
```

### Integration Testing
```bash
mvn verify
```

## Troubleshooting

### GCS Authentication Issues
- Ensure service account has `roles/storage.objectAdmin` role
- Verify `GOOGLE_APPLICATION_CREDENTIALS` points to valid service account key
- Check that Workload Identity is properly configured in GKE

### Database Connection Issues
- Verify Cloud SQL instance is running
- Check firewall rules allow connections
- Ensure service account has `roles/cloudsql.client` role
- Verify connection string format

### File Upload Issues
- Check GCS bucket exists and is accessible
- Verify bucket permissions
- Check bucket location matches application region for best performance

## Migration Checklist

- [x] Replace file system operations with GCS
- [x] Externalize configuration to environment variables
- [x] Remove static initializers with I/O
- [x] Standardize on UTC timestamps
- [x] Configure connection pooling
- [x] Add graceful shutdown
- [x] Update logging for cloud environments
- [x] Add GCS dependencies
- [x] Create cloud configuration classes
- [x] Document deployment procedures

## Support

For issues or questions:
- Check Cloud Logging for application logs
- Review GCS bucket permissions
- Verify environment variables are set correctly
- Check Cloud SQL connection settings
