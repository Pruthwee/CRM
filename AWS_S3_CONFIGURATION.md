# AWS S3 Configuration for CRM Application

## Overview
The CRM application has been migrated from local file system storage to Amazon S3 for cloud-native file storage. This ensures data durability, availability, and scalability in containerized and serverless environments.

## Changes Made

### 1. S3 Storage Service
- **File**: `src/main/java/crm/service/S3StorageService.java`
- **Purpose**: Manages file uploads to Amazon S3
- **Features**:
  - Uses AWS SDK v2 for S3 operations
  - Supports IAM role-based authentication (recommended for AWS environments)
  - Falls back to environment variables for credentials
  - Configurable bucket name and region

### 2. PDF Controller Updates
- **File**: `src/main/java/crm/controller/PdfController.java`
- **Changes**:
  - Removed local file system write operations (`FileOutputStream`)
  - PDF files are now generated in-memory using `ByteArrayOutputStream`
  - Files are uploaded directly to S3 after generation
  - S3 URLs are stored in the database for reference
  - Added timestamp prefix to ensure unique file names

### 3. Configuration
- **File**: `src/main/resources/application.properties`
- **Properties**:
  - `aws.s3.bucket.name`: S3 bucket name (default: crm-data-bucket)
  - `aws.region`: AWS region (default: us-east-1)

## Prerequisites

### 1. S3 Bucket Creation
Create an S3 bucket in your AWS account:
```bash
aws s3 mb s3://crm-data-bucket --region us-east-1
```

### 2. IAM Permissions
The application requires the following IAM permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
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

### 3. AWS Credentials Configuration

#### Option A: IAM Roles (Recommended for AWS Environments)
When running in AWS (ECS, EKS, EC2, Lambda), attach an IAM role with the above permissions to your service. The application will automatically use the role credentials.

#### Option B: Environment Variables (For Local Development)
Set the following environment variables:
```bash
export AWS_ACCESS_KEY_ID=your-access-key-id
export AWS_SECRET_ACCESS_KEY=your-secret-access-key
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=crm-data-bucket
```

#### Option C: AWS CLI Configuration (For Local Development)
Configure AWS CLI:
```bash
aws configure
```

## Configuration Options

### Environment Variables
The application supports the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `S3_BUCKET_NAME` | S3 bucket name for file storage | crm-data-bucket |
| `AWS_REGION` | AWS region where the bucket is located | us-east-1 |
| `AWS_ACCESS_KEY_ID` | AWS access key (if not using IAM roles) | - |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key (if not using IAM roles) | - |

### Application Properties
You can also override these in `application.properties`:
```properties
aws.s3.bucket.name=my-custom-bucket
aws.region=us-west-2
```

## File Storage Structure
PDF files are stored in S3 with the following structure:
```
s3://crm-data-bucket/
  └── pdfs/
      ├── 20240115-143022-document1.pdf
      ├── 20240115-143045-document2.pdf
      └── ...
```

Files are prefixed with a timestamp (yyyyMMdd-HHmmss) to ensure uniqueness.

## Benefits of S3 Storage

1. **Durability**: 99.999999999% (11 9's) durability
2. **Availability**: 99.99% availability SLA
3. **Scalability**: Automatically scales to handle any amount of data
4. **Cost-Effective**: Pay only for what you use
5. **Cloud-Native**: Works seamlessly in containerized environments
6. **No Data Loss**: Files persist across container restarts and scaling events
7. **Multi-AZ**: Data is automatically replicated across multiple availability zones

## Testing

### Local Testing
1. Configure AWS credentials (see Option B or C above)
2. Create the S3 bucket
3. Run the application
4. Generate a PDF through the web interface
5. Verify the file appears in S3:
   ```bash
   aws s3 ls s3://crm-data-bucket/pdfs/
   ```

### AWS Environment Testing
1. Deploy the application to AWS (ECS, EKS, etc.)
2. Ensure the IAM role is attached
3. Generate a PDF through the web interface
4. Verify the file appears in S3

## Troubleshooting

### Issue: "Failed to initialize S3 client"
- **Cause**: Missing AWS credentials or incorrect configuration
- **Solution**: Verify IAM role is attached or environment variables are set correctly

### Issue: "Access Denied" when uploading to S3
- **Cause**: Insufficient IAM permissions
- **Solution**: Verify the IAM role/user has `s3:PutObject` permission for the bucket

### Issue: "Bucket does not exist"
- **Cause**: S3 bucket not created or incorrect bucket name
- **Solution**: Create the bucket or verify the bucket name in configuration

### Issue: "Region mismatch"
- **Cause**: Application configured for different region than bucket
- **Solution**: Ensure `aws.region` matches the bucket's region

## Migration Notes

### Before (Local File System)
```java
// Old code - writes to local file system
PdfWriter.getInstance(document, new FileOutputStream(fileName));
```

### After (S3 Storage)
```java
// New code - generates in memory and uploads to S3
ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
PdfWriter.getInstance(document, outputStream);
// ... generate PDF ...
s3StorageService.uploadFile(s3Key, outputStream.toByteArray(), "application/pdf");
```

## Security Best Practices

1. **Use IAM Roles**: Always use IAM roles in AWS environments instead of access keys
2. **Least Privilege**: Grant only the minimum required S3 permissions
3. **Bucket Policies**: Configure bucket policies to restrict access
4. **Encryption**: Enable S3 server-side encryption (SSE-S3 or SSE-KMS)
5. **Versioning**: Enable S3 versioning for data protection
6. **Access Logging**: Enable S3 access logging for audit trails

## Additional Resources

- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [IAM Roles for Amazon ECS Tasks](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-iam-roles.html)
- [IAM Roles for Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html)
