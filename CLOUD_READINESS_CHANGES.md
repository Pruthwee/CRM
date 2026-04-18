# Cloud Readiness Transformation - GCP Migration

## Overview
This document describes the cloud-native transformations applied to make the CRM application fully compatible with Google Cloud Platform (GCP) deployment.

## Transformations Applied

### 1. Hard-coded File Paths (cr-java-0061)
**File**: `src/main/java/crm/utils/ReadDataUtils.java`

**Issue**: Application contained absolute file paths that reference specific locations on the host file system.

**Remediation**: Use environment variables with Secret Manager for dynamic paths
- Replaced hard-coded file paths with environment variables (`GCS_BUCKET_NAME`, `FILE_PATH_PREFIX`)
- Integrated Google Cloud Storage (GCS) for file retrieval
- Files are downloaded from GCS to ephemeral local storage
- Temporary files are automatically cleaned up on JVM exit
- Added comprehensive error handling and logging

**Environment Variables Required**:
- `GCS_BUCKET_NAME`: Name of the GCS bucket containing files
- `FILE_PATH_PREFIX`: Optional prefix for file paths in the bucket
- `DEFAULT_FILE_NAME`: Default filename for legacy method calls

---

### 2. Local File System Write Operations (cr-java-0062)
**File**: `src/main/java/crm/controller/PdfController.java`

**Issue**: Application performed direct write operations to local file system for PDF storage.

**Remediation**: Replace file writes with Firestore for structured data persistence
- PDF generation now occurs in-memory using `ByteArrayOutputStream`
- PDF content is stored as base64-encoded binary data in Firestore
- Metadata (filename, creation time, size) stored alongside content
- Each PDF document receives a unique UUID as Firestore document ID
- Application database stores reference to Firestore document ID
- Eliminates data loss from container restarts

**Firestore Collection**: `pdf_documents`

**Document Structure**:
```json
{
  "fileName": "example.pdf",
  "content": "Original text content",
  "pdfBase64": "Base64-encoded PDF binary",
  "createdAt": "2024-01-15T10:30:00Z",
  "size": 12345
}
```

---

### 3. Java.io.File Usage for Data Storage (cr-java-0063)
**File**: `src/main/java/crm/csv/CSVTest.java`

**Issue**: Application used Java File API for persistent data storage operations.

**Remediation**: Implement hybrid storage strategy with local caching
- CSV files stored durably in Google Cloud Storage (GCS)
- Files downloaded to ephemeral local disk for processing (performance optimization)
- Local cache automatically cleaned up after processing
- Supports read-heavy workloads with optimal performance
- Environment-driven configuration for bucket and file paths

**Environment Variables Required**:
- `GCS_BUCKET_NAME`: GCS bucket containing CSV files
- `CSV_FILE_PATH`: Path to CSV file within the bucket

---

### 4 & 5. Clock/Time Dependencies (cr-java-0111)
**File**: `src/main/java/crm/controller/DateTimeTestController.java`

**Issue**: Application relied on server-local timezone settings without considering distributed cloud environments.

**Remediation**: Standardize time handling with NTP synchronization and audit logging
- All timestamps use UTC timezone (`Clock.systemUTC()`)
- Implemented clock drift detection with configurable threshold (1000ms)
- Added health check endpoint for monitoring clock synchronization
- Comprehensive audit logging with UTC timestamps
- Validates clock sync on every request
- Warns when clock drift exceeds threshold

**Key Changes**:
- Uses `Clock.systemUTC()` for all time operations
- `LocalDateTime.now(UTC_CLOCK)` instead of `LocalDateTime.now()`
- `LocalDate.now(UTC_CLOCK)` instead of `LocalDate.now()`
- Added `validateClockSync()` method for drift detection
- New health check endpoint: `/date/health/clock`

---

### 6. Synchronous Blocking Operations (cr-java-0099)
**File**: `src/main/java/crm/csv/CSVTest.java`

**Issue**: Application performed synchronous blocking I/O operations for file processing.

**Remediation**: Implement async processing with Cloud Tasks and Pub/Sub pattern
- CSV processing now uses `CompletableFuture` for asynchronous execution
- Dedicated thread pool (`ExecutorService`) for background processing
- Non-blocking I/O improves throughput and resource utilization
- Results handled via callback pattern (`.thenAccept()`)
- Error handling via `.exceptionally()` callback
- Suitable for integration with Cloud Tasks or Pub/Sub for long-running operations

**Async Processing Pattern**:
```java
CompletableFuture<List<Object[]>> futureResults = processCSVAsync(cachedFile, filterValue);
futureResults.thenAccept(results -> {
    // Handle results asynchronously
}).exceptionally(ex -> {
    // Handle errors
    return null;
});
```

---

## Configuration Changes

### application.properties
Updated to use environment variables for all configuration:

**Database Configuration**:
- `DATABASE_URL`: JDBC connection string (supports Cloud SQL)
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `DB_POOL_SIZE`: HikariCP maximum pool size (default: 10)
- `DB_MIN_IDLE`: Minimum idle connections (default: 2)
- `DB_CONNECTION_TIMEOUT`: Connection timeout in ms (default: 30000)

**Server Configuration**:
- `PORT`: Server port (default: 8080, GCP Cloud Run uses this)

**GCP Configuration**:
- `GCS_BUCKET_NAME`: Google Cloud Storage bucket name
- `FILE_PATH_PREFIX`: Optional prefix for file paths
- `GCP_PROJECT_ID`: GCP project ID for Firestore
- `CSV_FILE_PATH`: Path to CSV files in GCS

**Logging Configuration**:
- `LOG_LEVEL`: Root logging level (default: INFO)
- `APP_LOG_LEVEL`: Application logging level (default: INFO)

**Time Zone**:
- All timestamps use UTC timezone
- Jackson JSON serialization uses UTC
- Hibernate JDBC timezone set to UTC

---

## Dependencies Added

### pom.xml
Added Google Cloud Platform client libraries:

```xml
<!-- Google Cloud Storage -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-storage</artifactId>
    <version>2.17.2</version>
</dependency>

<!-- Google Cloud Firestore -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-firestore</artifactId>
    <version>3.7.8</version>
</dependency>

<!-- Google Cloud Secret Manager -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-secretmanager</artifactId>
    <version>2.16.0</version>
</dependency>
```

---

## New Files Created

### GcpConfiguration.java
**Path**: `src/main/java/crm/config/GcpConfiguration.java`

Spring configuration class that provides beans for:
- Google Cloud Storage client
- Firestore client
- Uses Application Default Credentials (ADC) for authentication

---

## Entity Changes

### Pdf.java
Added field to store Firestore document reference:
```java
@Column(name = "firestore_document_id")
private String firestoreDocumentId;
```

---

## Deployment Requirements

### Environment Variables
The following environment variables must be configured in GCP deployment:

**Required**:
- `DATABASE_URL`: Cloud SQL connection string
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password (use Secret Manager)
- `GCS_BUCKET_NAME`: GCS bucket for file storage
- `GCP_PROJECT_ID`: GCP project ID

**Optional**:
- `PORT`: Server port (default: 8080)
- `FILE_PATH_PREFIX`: Prefix for file paths in GCS
- `CSV_FILE_PATH`: Path to CSV files
- `DB_POOL_SIZE`: Connection pool size
- `LOG_LEVEL`: Logging level

### GCP Services Required
1. **Cloud Storage**: For file storage (CSV, uploaded files)
2. **Firestore**: For PDF document storage
3. **Cloud SQL** (optional): For relational database
4. **Secret Manager** (recommended): For sensitive configuration

### IAM Permissions Required
The service account running the application needs:
- `roles/storage.objectViewer`: Read files from GCS
- `roles/datastore.user`: Read/write Firestore documents
- `roles/cloudsql.client`: Connect to Cloud SQL (if used)
- `roles/secretmanager.secretAccessor`: Access secrets (if used)

---

## Testing Recommendations

### Local Testing
1. Set up GCP credentials: `gcloud auth application-default login`
2. Create GCS bucket and upload test files
3. Set environment variables in IDE or shell
4. Run application and verify GCS/Firestore connectivity

### Cloud Testing
1. Deploy to Cloud Run or GKE
2. Configure environment variables in deployment
3. Verify service account has required IAM permissions
4. Test file upload/download operations
5. Monitor logs in Cloud Logging
6. Check Firestore console for stored documents

---

## Monitoring and Observability

### Logging
- All cloud operations logged with structured format
- UTC timestamps for consistency
- Error conditions logged with full context
- Use Cloud Logging for centralized log aggregation

### Health Checks
- Clock synchronization health check: `/date/health/clock`
- Spring Actuator endpoints: `/appinfo/health`, `/appinfo/metrics`

### Metrics to Monitor
- GCS download latency
- Firestore write latency
- Clock drift values
- Database connection pool utilization
- CSV processing duration

---

## Migration Checklist

- [x] Replace hard-coded file paths with GCS integration
- [x] Replace local file writes with Firestore storage
- [x] Implement hybrid storage strategy for CSV processing
- [x] Standardize time handling with UTC
- [x] Implement async processing for I/O operations
- [x] Add GCP client library dependencies
- [x] Configure environment variables
- [x] Create GCP configuration beans
- [x] Update application.properties for cloud deployment
- [x] Add comprehensive logging
- [x] Document all changes

---

## 12-Factor App Compliance

This transformation ensures compliance with 12-factor app principles:

1. **Codebase**: Single codebase tracked in version control
2. **Dependencies**: All dependencies explicitly declared in pom.xml
3. **Config**: Configuration stored in environment variables
4. **Backing Services**: GCS, Firestore, Cloud SQL treated as attached resources
5. **Build, Release, Run**: Strict separation maintained
6. **Processes**: Application is stateless (no local file dependencies)
7. **Port Binding**: Server port configurable via environment variable
8. **Concurrency**: Async processing supports horizontal scaling
9. **Disposability**: Fast startup, graceful shutdown, ephemeral storage
10. **Dev/Prod Parity**: Same code runs in all environments
11. **Logs**: Structured logging to stdout for cloud aggregation
12. **Admin Processes**: Separate admin tasks from application code

---

## Support and Troubleshooting

### Common Issues

**Issue**: "GCS_BUCKET_NAME environment variable is not set"
- **Solution**: Set the `GCS_BUCKET_NAME` environment variable

**Issue**: "File not found in GCS"
- **Solution**: Verify file exists in bucket and path is correct

**Issue**: "Failed to store PDF in Firestore"
- **Solution**: Check service account has `roles/datastore.user` permission

**Issue**: "Clock drift detected"
- **Solution**: Verify NTP is enabled on compute instances

### Debug Mode
Enable debug logging:
```properties
logging.level.crm=DEBUG
```

---

## Conclusion

The application is now fully cloud-ready and compatible with GCP deployment. All file system dependencies have been replaced with cloud-native storage services, time handling is standardized for distributed systems, and I/O operations are optimized for cloud environments.
