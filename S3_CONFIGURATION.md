# AWS S3 Configuration Guide

## Overview
This application has been migrated from local file system storage to Amazon S3 for cloud-native, durable file storage. All PDF generation operations now store files in S3 instead of the local file system.

## Cloud Readiness Fix Applied
**Issue**: Local file system write operations (blocker cr-java-0062)
**Remediation**: Replaced local file writes with Amazon S3 for durable storage

## Configuration

### Environment Variables
Set the following environment variables in your cloud deployment:

```bash
# Required: S3 bucket name for storing PDF files
AWS_S3_BUCKET_NAME=your-bucket-name

# Optional: AWS region (defaults to us-east-1)
AWS_REGION=us-east-1

# AWS Credentials (if not using IAM roles)
AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key
```

### IAM Role Configuration (Recommended for AWS)
For applications running on AWS (EC2, ECS, EKS, Lambda), use IAM roles instead of access keys:

1. Create an IAM role with the following S3 permissions:
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
        "arn:aws:s3:::your-bucket-name/*",
        "arn:aws:s3:::your-bucket-name"
      ]
    }
  ]
}
```

2. Attach the IAM role to your compute resource (EC2 instance, ECS task, EKS pod)

3. The application will automatically use the IAM role credentials via `DefaultAWSCredentialsProviderChain`

### S3 Bucket Setup

1. Create an S3 bucket:
```bash
aws s3 mb s3://your-bucket-name --region us-east-1
```

2. Configure bucket versioning (optional but recommended):
```bash
aws s3api put-bucket-versioning \
  --bucket your-bucket-name \
  --versioning-configuration Status=Enabled
```

3. Configure bucket lifecycle policies (optional):
```bash
# Example: Delete files older than 90 days
aws s3api put-bucket-lifecycle-configuration \
  --bucket your-bucket-name \
  --lifecycle-configuration file://lifecycle.json
```

## Application Properties
The following properties can be configured in `application.properties` or via environment variables:

```properties
# S3 bucket name (can be overridden by AWS_S3_BUCKET_NAME env var)
aws.s3.bucket.name=crm-pdf-storage

# AWS region (can be overridden by AWS_REGION env var)
aws.s3.region=us-east-1
```

## Benefits of S3 Migration

1. **Data Durability**: S3 provides 99.999999999% (11 9's) durability
2. **Scalability**: Automatically scales to handle any number of files
3. **Availability**: Files are accessible from any container instance
4. **Container-Ready**: No data loss when containers restart or scale
5. **Cost-Effective**: Pay only for storage used
6. **Security**: Supports encryption at rest and in transit

## Testing

### Local Development
For local development, you can use:
1. AWS credentials file (`~/.aws/credentials`)
2. Environment variables
3. LocalStack for S3 emulation

### Cloud Deployment
In cloud environments, use IAM roles for secure, credential-free access to S3.

## Troubleshooting

### Common Issues

1. **Access Denied Error**
   - Verify IAM role/credentials have correct S3 permissions
   - Check bucket name is correct
   - Ensure bucket exists in the specified region

2. **Bucket Not Found**
   - Verify bucket name in configuration
   - Ensure bucket exists in the correct region
   - Check AWS_REGION environment variable

3. **Connection Timeout**
   - Verify network connectivity to S3
   - Check security group rules allow outbound HTTPS (port 443)
   - Verify VPC endpoints if using private subnets

## Migration Notes

### Changes Made
1. **PdfController.java**: 
   - Replaced `FileOutputStream` with `ByteArrayOutputStream`
   - Added S3Service dependency
   - Upload PDF bytes to S3 instead of local file system
   - Return S3 URL instead of local file path

2. **S3Service.java** (New):
   - Cloud-native service for S3 operations
   - Uses `DefaultAWSCredentialsProviderChain` for flexible credential management
   - Provides methods for upload, delete, and existence checks

3. **pom.xml**:
   - Added AWS SDK for S3 dependencies

4. **application.properties**:
   - Added S3 configuration properties with environment variable overrides

### Backward Compatibility
This migration is a breaking change for local file system operations. All PDF files are now stored in S3 and accessed via S3 URLs.

## Security Best Practices

1. **Never commit AWS credentials** to source control
2. **Use IAM roles** when running on AWS
3. **Enable S3 bucket encryption** at rest
4. **Use HTTPS** for all S3 operations (enabled by default)
5. **Implement bucket policies** to restrict access
6. **Enable S3 access logging** for audit trails
7. **Use S3 Block Public Access** to prevent accidental public exposure

## Additional Resources
- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [AWS SDK for Java Documentation](https://docs.aws.amazon.com/sdk-for-java/)
- [IAM Roles for EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/iam-roles-for-amazon-ec2.html)
- [IAM Roles for ECS Tasks](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-iam-roles.html)
