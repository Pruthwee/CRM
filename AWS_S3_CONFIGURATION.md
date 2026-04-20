# AWS S3 Configuration for Cloud Deployment

## Overview
This application has been migrated from local file system storage to Amazon S3 for PDF file storage. This ensures data durability, availability, and scalability in cloud and containerized environments.

## Changes Made
1. **PdfController.java**: Replaced `FileOutputStream` with S3 upload using `ByteArrayOutputStream`
2. **S3Service.java**: New service class for handling S3 operations
3. **pom.xml**: Added AWS SDK for S3 dependency
4. **application.properties**: Added S3 configuration properties

## AWS Configuration Requirements

### 1. S3 Bucket Setup
Create an S3 bucket in your AWS account:
```bash
aws s3 mb s3://crm-pdf-storage --region us-east-1
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
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::crm-pdf-storage",
        "arn:aws:s3:::crm-pdf-storage/*"
      ]
    }
  ]
}
```

### 3. Environment Variables
Set the following environment variables for your deployment:

```bash
# Required
AWS_S3_BUCKET_NAME=crm-pdf-storage
AWS_REGION=us-east-1

# AWS Credentials (if not using IAM roles)
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

### 4. Deployment Options

#### Option A: Using IAM Roles (Recommended for ECS/EKS)
- Attach the IAM policy to your ECS task role or EKS service account
- No need to set AWS_ACCESS_KEY_ID or AWS_SECRET_ACCESS_KEY
- The application uses `DefaultAWSCredentialsProviderChain` which automatically detects IAM roles

#### Option B: Using Environment Variables
- Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY as environment variables
- Suitable for EC2 instances or local testing

#### Option C: Using AWS Credentials File
- Place credentials in `~/.aws/credentials`
- Suitable for development environments

## Testing
To test the S3 integration locally:

1. Configure AWS credentials:
```bash
aws configure
```

2. Set environment variables:
```bash
export AWS_S3_BUCKET_NAME=crm-pdf-storage
export AWS_REGION=us-east-1
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. Generate a PDF through the web interface at `/pdf-generator`

5. Verify the file was uploaded to S3:
```bash
aws s3 ls s3://crm-pdf-storage/pdfs/
```

## Benefits of S3 Migration
- ✅ **Durability**: 99.999999999% (11 9's) durability
- ✅ **Availability**: 99.99% availability SLA
- ✅ **Scalability**: Automatic scaling without capacity planning
- ✅ **Container-friendly**: No data loss on container restarts
- ✅ **Multi-region**: Can replicate data across regions
- ✅ **Cost-effective**: Pay only for storage used

## Troubleshooting

### Issue: "Access Denied" errors
- Verify IAM permissions are correctly configured
- Check that the bucket name matches the configuration
- Ensure AWS credentials are properly set

### Issue: "Bucket does not exist"
- Create the S3 bucket in the correct region
- Verify the bucket name in application.properties

### Issue: "Unable to load AWS credentials"
- Check that AWS credentials are configured
- Verify IAM role is attached (for ECS/EKS)
- Ensure environment variables are set correctly
