# Cloud Deployment Guide - CRM Application

## Overview
This CRM application has been modernized for cloud deployment on AWS. All hardcoded file paths, desktop GUI dependencies, and local file system operations have been replaced with cloud-native alternatives.

## Cloud Readiness Fixes Applied

### 1. Hard-coded File Paths (CRITICAL - RESOLVED)
**Issue**: Application used `javax.swing.JFileChooser` with hardcoded file paths, which is incompatible with cloud environments.

**Fix Applied**:
- Replaced `ReadDataUtils.java` desktop file chooser with cloud-native resource loading
- Implemented classpath resource loading for packaged files
- Added support for AWS S3 integration (optional)
- Created `FileStorageService` for abstracted file storage operations

**Files Modified**:
- `src/main/java/crm/utils/ReadDataUtils.java` - Replaced JFileChooser with classpath resources
- `src/main/java/crm/controller/CSVController.java` - Removed desktop file chooser usage
- `src/main/java/crm/csv/CSVTest.java` - Updated to use classpath resources
- `src/main/java/crm/service/storage/FileStorageService.java` - NEW: Cloud storage abstraction

### 2. Configuration Management (RESOLVED)
**Issue**: Hardcoded database credentials and configuration values.

**Fix Applied**:
- Replaced all hardcoded values with environment variables
- Added connection pooling configuration (HikariCP)
- Configured secure actuator endpoints
- Added file upload size limits

**Files Modified**:
- `src/main/resources/application.properties` - All values now use environment variables

### 3. Dependencies (RESOLVED)
**Issue**: Missing cloud-native dependencies.

**Fix Applied**:
- Added AWS SDK for S3 (file storage)
- Added AWS SDK for Secrets Manager (credential management)
- Added SLF4J for structured logging
- Configured executable JAR packaging

**Files Modified**:
- `pom.xml` - Added AWS SDK dependencies and cloud-ready configuration

## AWS Deployment Instructions

### Prerequisites
1. AWS Account with appropriate permissions
2. AWS CLI installed and configured
3. Docker installed (for containerization)
4. Maven 3.x installed

### Step 1: Build the Application
```bash
mvn clean package -DskipTests
```

This creates an executable JAR: `target/crm-0.0.1-SNAPSHOT.jar`

### Step 2: Set Up AWS Resources

#### 2.1 Create RDS MySQL Database
```bash
aws rds create-db-instance \
  --db-instance-identifier crm-database \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password <YOUR_SECURE_PASSWORD> \
  --allocated-storage 20 \
  --vpc-security-group-ids <YOUR_SECURITY_GROUP> \
  --db-name crm
```

#### 2.2 Create S3 Bucket (Optional - for file storage)
```bash
aws s3 mb s3://crm-files-bucket-<YOUR_UNIQUE_ID>
```

#### 2.3 Create IAM Role for EC2/ECS
Create an IAM role with the following policies:
- `AmazonS3FullAccess` (or custom policy for your S3 bucket)
- `SecretsManagerReadWrite` (for database credentials)
- `CloudWatchLogsFullAccess` (for logging)

### Step 3: Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database Configuration (AWS RDS)
export DB_URL=jdbc:mysql://crm-database.xxxxx.us-east-1.rds.amazonaws.com:3306/crm?useSSL=true
export DB_USERNAME=admin
export DB_PASSWORD=<YOUR_SECURE_PASSWORD>
export DDL_AUTO=update

# Connection Pool Configuration
export DB_POOL_SIZE=20
export DB_POOL_MIN_IDLE=5
export DB_CONNECTION_TIMEOUT=30000

# Server Configuration
export PORT=8080

# AWS Configuration
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=crm-files-bucket-<YOUR_UNIQUE_ID>

# File Storage Configuration
export FILE_STORAGE_TYPE=s3  # or "local" for development

# Logging Configuration
export LOG_LEVEL_ROOT=INFO
export LOG_LEVEL_APP=INFO

# Security Configuration
export MANAGEMENT_SECURITY_ENABLED=true
export MANAGEMENT_ENDPOINTS=health,info,metrics

# Thymeleaf Configuration
export THYMELEAF_CACHE=true
```

### Step 4: Deployment Options

#### Option A: Deploy to AWS Elastic Beanstalk
```bash
# Install EB CLI
pip install awsebcli

# Initialize Elastic Beanstalk
eb init -p java-8 crm-application --region us-east-1

# Create environment
eb create crm-production

# Set environment variables
eb setenv DB_URL=jdbc:mysql://... DB_USERNAME=admin DB_PASSWORD=...

# Deploy
eb deploy
```

#### Option B: Deploy to AWS ECS (Fargate)

1. Create Dockerfile:
```dockerfile
FROM openjdk:8-jre-alpine
VOLUME /tmp
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

2. Build and push Docker image:
```bash
# Build image
docker build -t crm-application .

# Tag for ECR
docker tag crm-application:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/crm-application:latest

# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/crm-application:latest
```

3. Create ECS Task Definition and Service (use AWS Console or CLI)

#### Option C: Deploy to AWS EC2

1. Launch EC2 instance (Amazon Linux 2)
2. Install Java 8:
```bash
sudo yum install java-1.8.0-openjdk
```

3. Copy JAR file to EC2:
```bash
scp -i your-key.pem target/crm-0.0.1-SNAPSHOT.jar ec2-user@<EC2_IP>:~/
```

4. Create systemd service:
```bash
sudo nano /etc/systemd/system/crm.service
```

```ini
[Unit]
Description=CRM Application
After=syslog.target

[Service]
User=ec2-user
ExecStart=/usr/bin/java -jar /home/ec2-user/crm-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Environment="DB_URL=jdbc:mysql://..."
Environment="DB_USERNAME=admin"
Environment="DB_PASSWORD=..."

[Install]
WantedBy=multi-user.target
```

5. Start service:
```bash
sudo systemctl enable crm
sudo systemctl start crm
```

### Step 5: Configure Load Balancer and Auto Scaling

1. Create Application Load Balancer
2. Configure target group pointing to port 8080
3. Set up Auto Scaling Group
4. Configure health checks: `/appinfo/health`

### Step 6: Set Up CloudWatch Monitoring

1. Configure CloudWatch Logs agent on EC2/ECS
2. Create CloudWatch alarms for:
   - CPU utilization > 80%
   - Memory utilization > 80%
   - HTTP 5xx errors
   - Database connection pool exhaustion

### Step 7: Security Best Practices

1. **Use AWS Secrets Manager for Database Credentials**:
```bash
aws secretsmanager create-secret \
  --name crm/database/credentials \
  --secret-string '{"username":"admin","password":"<YOUR_PASSWORD>"}'
```

2. **Configure Security Groups**:
   - Application: Allow inbound 8080 from Load Balancer only
   - Database: Allow inbound 3306 from Application security group only
   - Load Balancer: Allow inbound 80/443 from 0.0.0.0/0

3. **Enable SSL/TLS**:
   - Use AWS Certificate Manager for SSL certificates
   - Configure HTTPS listener on Load Balancer

4. **Enable VPC Flow Logs** for network monitoring

## File Upload Implementation

For CSV import functionality in cloud environments, implement a REST endpoint:

```java
@PostMapping("/customers/import")
public ResponseEntity<?> importCustomers(@RequestParam("file") MultipartFile file) {
    try {
        // Save to S3 or process in-memory
        File tempFile = File.createTempFile("upload-", ".csv");
        file.transferTo(tempFile);
        
        // Process CSV
        CSVReader reader = new CSVReader(new FileReader(tempFile));
        // ... process data ...
        
        // Clean up
        tempFile.delete();
        
        return ResponseEntity.ok("Import successful");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Import failed: " + e.getMessage());
    }
}
```

## Monitoring and Logging

### CloudWatch Logs
Application logs are automatically sent to CloudWatch when deployed on ECS/Elastic Beanstalk.

### Actuator Endpoints
- Health: `http://<your-domain>/appinfo/health`
- Info: `http://<your-domain>/appinfo/info`
- Metrics: `http://<your-domain>/appinfo/metrics`

### Custom Metrics
Add custom metrics using Micrometer (included in Spring Boot Actuator):
```java
@Autowired
private MeterRegistry meterRegistry;

public void recordCustomMetric() {
    meterRegistry.counter("crm.custom.metric").increment();
}
```

## Troubleshooting

### Issue: Database Connection Timeout
**Solution**: Check security group rules, verify RDS endpoint, increase connection timeout

### Issue: File Upload Fails
**Solution**: Check S3 bucket permissions, verify IAM role has S3 access

### Issue: Application Won't Start
**Solution**: Check CloudWatch logs, verify environment variables are set correctly

### Issue: High Memory Usage
**Solution**: Adjust JVM heap size: `-Xmx512m -Xms256m`

## Cost Optimization

1. Use RDS Reserved Instances for production
2. Enable S3 Intelligent-Tiering for file storage
3. Use Auto Scaling to scale down during low traffic
4. Enable CloudWatch Logs retention policies (7-30 days)
5. Use AWS Cost Explorer to monitor spending

## Rollback Procedure

### Elastic Beanstalk
```bash
eb deploy --version <previous-version>
```

### ECS
Update service to use previous task definition revision

### EC2
Keep previous JAR file and switch systemd service to point to it

## Support and Maintenance

- Monitor CloudWatch dashboards daily
- Review application logs weekly
- Update dependencies monthly
- Perform security patches as needed
- Test disaster recovery procedures quarterly

## Additional Resources

- [AWS Elastic Beanstalk Documentation](https://docs.aws.amazon.com/elasticbeanstalk/)
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [Spring Boot on AWS](https://spring.io/guides/gs/spring-boot-aws/)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
