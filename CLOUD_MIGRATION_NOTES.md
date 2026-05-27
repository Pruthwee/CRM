# Cloud Migration Notes - AWS Readiness

## Overview
This application has been migrated to be cloud-ready for AWS deployment. All cloud readiness blockers have been resolved.

## Changes Applied

### 1. File System Dependencies → Amazon S3 Storage

#### Blocker: cr-java-0061 - Hard-coded File Paths
**File:** `ReadDataUtils.java`
- **Before:** Used JFileChooser with local file system paths
- **After:** Migrated to AWS S3 SDK v2 for cloud-native file operations
- **Changes:**
  - Replaced `java.io.File` with S3Client operations
  - Added methods: `readFileFromS3()`, `listFilesFromS3()`, `fileExistsInS3()`
  - Uses environment variables for bucket configuration

#### Blocker: cr-java-0062 - Local File System Write Operations
**File:** `PdfController.java`
- **Before:** Wrote PDF files to local file system using `FileOutputStream`
- **After:** Generates PDFs in memory and uploads to S3
- **Changes:**
  - Replaced `FileOutputStream` with `ByteArrayOutputStream`
  - Added S3 upload using `PutObjectRequest`
  - Stores S3 key reference in database instead of file path
  - Uses environment variables: `AWS_S3_BUCKET`, `AWS_S3_PDF_PREFIX`

#### Blocker: cr-java-0063 - Java.io.File Usage for Data Storage
**File:** `CSVTest.java`
- **Before:** Used `java.io.File` and `FileReader` for CSV processing
- **After:** Reads CSV files directly from S3
- **Changes:**
  - Replaced File operations with S3 `GetObjectRequest`
  - Uses `ResponseInputStream` for streaming S3 objects
  - Added `readCsvFromS3()` method for cloud-native CSV processing
  - Configurable via environment variables

### 2. Time/Clock Dependencies → java.time API with UTC

#### Blockers: cr-java-0111 - Clock/Time Dependencies (Lines 19-20)
**File:** `DateTimeTestController.java`
- **Before:** Used `java.util.Date` which relies on server-local timezone
- **After:** Migrated to `java.time` API with UTC standardization
- **Changes:**
  - Removed `java.util.Date` usage
  - Added `Clock.systemUTC()` for consistent time across regions
  - Uses `Instant`, `ZonedDateTime`, `LocalDateTime` with UTC clock
  - Eliminates timezone inconsistencies in distributed environments

## New Dependencies Added

### AWS SDK for Java v2
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.17.100</version>
</dependency>
```

## Configuration Files Updated

### application.properties
Added cloud-native configuration with environment variable support:
- `aws.region` - AWS region (default: us-east-1)
- `aws.s3.bucket.name` - S3 bucket for storage
- `aws.s3.pdf.prefix` - S3 prefix for PDF files
- `aws.s3.csv.prefix` - S3 prefix for CSV files
- Database credentials now use environment variables
- Hibernate timezone set to UTC

### New Configuration Class
**File:** `AwsS3Config.java`
- Spring configuration for S3Client bean
- Uses `DefaultCredentialsProvider` for cloud-native authentication
- Supports IAM roles, instance profiles, and container credentials

## Environment Variables Required

### AWS Configuration
- `AWS_REGION` - AWS region (default: us-east-1)
- `AWS_S3_BUCKET` - S3 bucket name for file storage
- `AWS_S3_PDF_PREFIX` - S3 prefix for PDF files (default: pdfs/)
- `AWS_S3_CSV_PREFIX` - S3 prefix for CSV files (default: csv/)

### Database Configuration
- `DATABASE_URL` - JDBC connection string
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password

### AWS Credentials (handled automatically in cloud)
- `AWS_ACCESS_KEY_ID` - For local development
- `AWS_SECRET_ACCESS_KEY` - For local development
- In AWS (EC2/ECS/EKS): Uses IAM roles automatically

## Deployment Considerations

### AWS Services Required
1. **Amazon S3** - For file storage (PDFs, CSVs)
2. **Amazon RDS** - For MySQL database (or Aurora)
3. **IAM Roles** - For secure access to S3 and RDS

### IAM Permissions Required
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket",
        "s3:HeadObject"
      ],
      "Resource": [
        "arn:aws:s3:::your-bucket-name/*",
        "arn:aws:s3:::your-bucket-name"
      ]
    }
  ]
}
```

### S3 Bucket Setup
1. Create S3 bucket in target AWS region
2. Configure bucket policy for application access
3. Set up lifecycle policies for cost optimization
4. Enable versioning for data durability (optional)

## Testing Locally

### Prerequisites
1. AWS CLI configured with credentials
2. S3 bucket created
3. Environment variables set

### Local Testing Steps
```bash
# Set environment variables
export AWS_REGION=us-east-1
export AWS_S3_BUCKET=your-test-bucket
export DATABASE_URL=jdbc:mysql://localhost:3306/crm
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=password

# Run application
mvn spring-boot:run
```

## Migration Benefits

### Cloud-Native Advantages
1. **Scalability** - S3 scales automatically, no disk space limits
2. **Durability** - 99.999999999% durability for stored files
3. **Availability** - Multi-AZ redundancy by default
4. **Cost-Effective** - Pay only for storage used
5. **Stateless** - Application can scale horizontally
6. **Time Consistency** - UTC standardization across all regions

### 12-Factor App Compliance
- ✅ Configuration via environment variables
- ✅ Stateless processes (no local file dependencies)
- ✅ Backing services (S3, RDS) as attached resources
- ✅ Disposability (fast startup, graceful shutdown)

## Verification Checklist

- [x] All file operations migrated to S3
- [x] No local file system dependencies
- [x] Time operations use UTC with java.time API
- [x] Configuration externalized to environment variables
- [x] AWS SDK dependencies added
- [x] S3Client configured with Spring
- [x] Database configuration uses environment variables
- [x] Application is stateless and horizontally scalable

## Next Steps

1. **Infrastructure Setup**
   - Provision S3 bucket
   - Set up RDS database
   - Configure IAM roles

2. **Deployment**
   - Build Docker image (separate workflow)
   - Deploy to ECS/EKS/Elastic Beanstalk
   - Configure environment variables

3. **Monitoring**
   - Set up CloudWatch logs
   - Configure S3 access logging
   - Monitor application metrics

## Support

For issues or questions about the cloud migration:
- Review AWS SDK documentation: https://docs.aws.amazon.com/sdk-for-java/
- Check S3 best practices: https://docs.aws.amazon.com/AmazonS3/latest/userguide/
