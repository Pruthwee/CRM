# S3 Migration Guide for CRM Application

## Overview
The CRM application has been migrated from local file system dependencies to Amazon S3 object storage for cloud compatibility.

## Changes Made

### 1. ReadDataUtils.java
- **Before**: Used `JFileChooser` (Swing GUI component) to select files from local file system
- **After**: Downloads files from Amazon S3 using AWS SDK for Java v2
- **Impact**: Application is now cloud-ready and doesn't require GUI or local file system access

### 2. Dependencies Added (pom.xml)
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.26</version>
</dependency>
```

### 3. Configuration
Environment variables for S3 configuration:
- `S3_BUCKET_NAME`: The S3 bucket name (default: `crm-data-bucket`)
- `AWS_REGION`: AWS region (default: `us-east-1`)
- AWS credentials: Use IAM roles (recommended) or environment variables

## Usage Changes

### Old Usage (Local File System)
```java
File document = ReadDataUtils.ReadFile("Select CSV file", null, "Only CSV Files", "csv");
```

### New Usage (S3)
```java
// Method signature remains the same for backward compatibility
// But parameters now have different meanings:
// - First parameter: S3 object key (e.g., "data/customers.csv")
// - Second parameter: S3 bucket name (null uses default from env var)
// - Third parameter: Description (ignored)
// - Fourth parameter: File extension for validation

File document = ReadDataUtils.ReadFile("data/customers.csv", null, "Only CSV Files", "csv");
```

### Alternative: Direct S3 InputStream
For better performance, use the new streaming method:
```java
InputStream stream = ReadDataUtils.getS3InputStream("crm-data-bucket", "data/customers.csv");
```

## Deployment Configuration

### AWS IAM Role (Recommended for EC2/ECS/EKS)
Attach an IAM role to your compute instance with the following policy:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::crm-data-bucket",
        "arn:aws:s3:::crm-data-bucket/*"
      ]
    }
  ]
}
```

### Environment Variables (Alternative)
If not using IAM roles, set these environment variables:
```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=crm-data-bucket
```

## Data Migration

### Upload CSV Files to S3
```bash
# Using AWS CLI
aws s3 cp local-file.csv s3://crm-data-bucket/data/customers.csv

# Upload entire directory
aws s3 sync ./local-data-dir s3://crm-data-bucket/data/
```

### S3 Bucket Structure (Recommended)
```
crm-data-bucket/
├── data/
│   ├── customers.csv
│   ├── contracts.csv
│   └── ...
├── imports/
│   └── ...
└── exports/
    └── ...
```

## Code Updates Required

### Files Using ReadDataUtils
The following files use `ReadDataUtils` and need their calling code updated:

1. **CSVController.java** (commented out code)
   - Update the S3 key parameter when uncommenting
   
2. **CSVTest.java**
   - Update to use S3 key instead of file dialog

Example update for CSVTest.java:
```java
// OLD: Opens file dialog (doesn't work in cloud)
File document = ReadDataUtils.ReadFile("Select CSV file", null, "Only CSV Files", "csv");

// NEW: Downloads from S3
File document = ReadDataUtils.ReadFile("data/test-data.csv", null, "Only CSV Files", "csv");
```

## Testing

### Local Testing with LocalStack
For local development, use LocalStack to simulate S3:
```bash
# Start LocalStack
docker run -d -p 4566:4566 localstack/localstack

# Configure to use LocalStack
export AWS_ENDPOINT_URL=http://localhost:4566
export S3_BUCKET_NAME=crm-data-bucket
export AWS_REGION=us-east-1
```

### Integration Testing
1. Create test S3 bucket
2. Upload test CSV files
3. Run application with test bucket configuration
4. Verify file downloads work correctly

## Troubleshooting

### Common Issues

1. **Access Denied Error**
   - Verify IAM role/credentials have S3 read permissions
   - Check bucket policy allows access
   - Verify bucket name and region are correct

2. **File Not Found**
   - Verify S3 key (path) is correct
   - Check file exists in bucket: `aws s3 ls s3://bucket-name/path/`

3. **Region Mismatch**
   - Ensure AWS_REGION matches bucket region
   - Check bucket region: `aws s3api get-bucket-location --bucket bucket-name`

## Benefits of S3 Migration

1. **Cloud-Native**: No dependency on local file system
2. **Scalable**: S3 handles any amount of data
3. **Durable**: 99.999999999% durability
4. **Accessible**: Files accessible from any cloud instance
5. **Secure**: Fine-grained access control with IAM
6. **Cost-Effective**: Pay only for storage used

## Next Steps

1. Upload existing CSV files to S3
2. Update calling code to use S3 keys instead of file dialogs
3. Configure IAM roles for production deployment
4. Test thoroughly in staging environment
5. Monitor S3 access logs and CloudWatch metrics
