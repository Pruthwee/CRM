# CRM Application - Cloud Ready for AWS

## Overview
This is a Customer Relationship Management (CRM) application built with Spring Boot, modernized and optimized for cloud deployment on AWS. The application has been refactored to eliminate desktop dependencies and hardcoded configurations, making it fully cloud-native.

## Cloud Readiness Status ✅

### Issues Resolved
- ✅ **Hard-coded File Paths**: Replaced desktop file chooser with cloud-native resource loading
- ✅ **Configuration Management**: All hardcoded values replaced with environment variables
- ✅ **File System Dependencies**: Implemented AWS S3 integration for file storage
- ✅ **Database Configuration**: Added connection pooling and externalized credentials
- ✅ **Logging**: Configured for cloud monitoring (CloudWatch compatible)
- ✅ **Containerization**: Added Docker support for ECS/EKS deployment
- ✅ **Security**: Implemented secure configuration management

## Technology Stack

### Core Technologies
- **Java**: 8
- **Spring Boot**: 1.5.10.RELEASE
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database persistence
- **Thymeleaf**: Server-side templating
- **MySQL**: Primary database (AWS RDS compatible)
- **H2**: In-memory database for testing

### Cloud Technologies
- **AWS SDK**: S3 for file storage, Secrets Manager for credentials
- **HikariCP**: Connection pooling (default in Spring Boot)
- **Docker**: Containerization for ECS/EKS
- **CloudWatch**: Logging and monitoring

### Additional Libraries
- **iText PDF**: PDF generation
- **Apache POI**: Excel file processing
- **OpenCSV**: CSV file processing
- **Lombok**: Reduce boilerplate code

## Project Structure

```
CRM-component-pruthwee/
├── src/
│   ├── main/
│   │   ├── java/crm/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── service/          # Business logic
│   │   │   │   └── storage/      # File storage service (S3)
│   │   │   ├── utils/            # Utility classes
│   │   │   ├── view/             # View resolvers
│   │   │   └── CrmApplication.java
│   │   └── resources/
│   │       ├── application.properties  # Cloud-ready configuration
│   │       ├── data/             # Classpath resources
│   │       ├── static/           # Static assets
│   │       └── templates/        # Thymeleaf templates
│   └── test/                     # Unit tests
├── pom.xml                       # Maven dependencies
├── Dockerfile                    # Container image definition
├── docker-compose.yml            # Local development setup
├── aws-deployment.yml            # AWS Elastic Beanstalk config
├── CLOUD_DEPLOYMENT_GUIDE.md    # Detailed deployment guide
└── README.md                     # This file
```

## Quick Start

### Prerequisites
- Java 8 or higher
- Maven 3.x
- Docker (optional, for containerized deployment)
- MySQL 5.7+ (or use H2 for testing)

### Local Development

1. **Clone the repository**
   ```bash
   cd /path/to/CRM-component-pruthwee
   ```

2. **Configure environment variables**
   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/crm?useSSL=false
   export DB_USERNAME=root
   export DB_PASSWORD=password
   export DDL_AUTO=update
   ```

3. **Build the application**
   ```bash
   mvn clean package
   ```

4. **Run the application**
   ```bash
   java -jar target/crm-0.0.1-SNAPSHOT.jar
   ```

5. **Access the application**
   - Application: http://localhost:8080
   - Health Check: http://localhost:8080/appinfo/health
   - Metrics: http://localhost:8080/appinfo/metrics

### Docker Development

1. **Build and run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

2. **Access the application**
   - Application: http://localhost:8080
   - MySQL: localhost:3306

3. **Stop the application**
   ```bash
   docker-compose down
   ```

## Configuration

### Environment Variables

All configuration is externalized via environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_URL` | Database JDBC URL | jdbc:mysql://localhost:3306/crm | Yes |
| `DB_USERNAME` | Database username | root | Yes |
| `DB_PASSWORD` | Database password | password | Yes |
| `DDL_AUTO` | Hibernate DDL mode | update | No |
| `PORT` | Server port | 8080 | No |
| `AWS_REGION` | AWS region | us-east-1 | No |
| `S3_BUCKET_NAME` | S3 bucket for files | crm-files-bucket | No |
| `FILE_STORAGE_TYPE` | Storage type (local/s3) | local | No |
| `LOG_LEVEL_ROOT` | Root log level | INFO | No |
| `LOG_LEVEL_APP` | Application log level | INFO | No |

### Database Configuration

#### Development (H2 In-Memory)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
```

#### Production (AWS RDS MySQL)
```properties
DB_URL=jdbc:mysql://your-rds-endpoint.rds.amazonaws.com:3306/crm?useSSL=true
DB_USERNAME=admin
DB_PASSWORD=<from-secrets-manager>
```

## AWS Deployment

### Option 1: AWS Elastic Beanstalk

```bash
# Install EB CLI
pip install awsebcli

# Initialize
eb init -p java-8 crm-application --region us-east-1

# Create environment
eb create crm-production

# Set environment variables
eb setenv DB_URL=jdbc:mysql://... DB_USERNAME=admin DB_PASSWORD=...

# Deploy
eb deploy
```

### Option 2: AWS ECS (Fargate)

```bash
# Build Docker image
docker build -t crm-application .

# Tag for ECR
docker tag crm-application:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-application:latest

# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-application:latest

# Create ECS task definition and service (via AWS Console or CLI)
```

### Option 3: AWS EC2

See [CLOUD_DEPLOYMENT_GUIDE.md](CLOUD_DEPLOYMENT_GUIDE.md) for detailed instructions.

## API Endpoints

### Customer Management
- `GET /customers` - Export all customers as CSV
- `GET /customers/{id}` - Export single customer as CSV
- `GET /customer/list` - List all customers (HTML)
- `GET /customer/show/{id}` - Show customer details
- `POST /customer/save` - Create/update customer
- `DELETE /customer/delete/{id}` - Delete customer

### User Management
- `GET /user/list` - List all users
- `GET /user/show/{id}` - Show user details
- `POST /user/save` - Create/update user
- `DELETE /user/delete/{id}` - Delete user

### Contract Management
- `GET /contract/list` - List all contracts
- `GET /contract/show/{id}` - Show contract details
- `POST /contract/save` - Create/update contract
- `DELETE /contract/delete/{id}` - Delete contract

### Export Endpoints
- `GET /export/customers/pdf` - Export customers as PDF
- `GET /export/customers/excel` - Export customers as Excel
- `GET /export/customers/csv` - Export customers as CSV

### Health & Monitoring
- `GET /appinfo/health` - Application health status
- `GET /appinfo/info` - Application information
- `GET /appinfo/metrics` - Application metrics

## File Upload Implementation

For cloud-compatible file uploads, use MultipartFile:

```java
@PostMapping("/upload/csv")
public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
    try {
        // Option 1: Process in-memory
        InputStream inputStream = file.getInputStream();
        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
        // Process CSV...
        
        // Option 2: Save to S3
        File tempFile = File.createTempFile("upload-", ".csv");
        file.transferTo(tempFile);
        fileStorageService.uploadFile(tempFile, "uploads/" + file.getOriginalFilename());
        
        return ResponseEntity.ok("Upload successful");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Upload failed");
    }
}
```

## Security

### Authentication
- Spring Security with form-based login
- User roles: ADMIN, USER
- Password encryption with BCrypt

### AWS Security Best Practices
1. Use IAM roles for EC2/ECS instances
2. Store credentials in AWS Secrets Manager
3. Enable VPC security groups
4. Use SSL/TLS for database connections
5. Enable CloudWatch logging
6. Implement least privilege access

## Monitoring

### CloudWatch Metrics
- CPU utilization
- Memory usage
- Request count
- Response time
- Error rate

### Application Metrics (Actuator)
- JVM metrics
- HTTP metrics
- Database connection pool metrics
- Custom business metrics

### Logging
- Structured logging (JSON format)
- Log levels: ERROR, WARN, INFO, DEBUG
- CloudWatch Logs integration
- Correlation IDs for distributed tracing

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## Troubleshooting

### Application Won't Start
- Check environment variables are set correctly
- Verify database connection
- Check CloudWatch logs for errors

### Database Connection Issues
- Verify RDS security group allows inbound traffic
- Check database credentials
- Verify RDS endpoint is correct

### File Upload Issues
- Check S3 bucket permissions
- Verify IAM role has S3 access
- Check file size limits

## Performance Optimization

### Database
- Connection pooling (HikariCP)
- Query optimization
- Proper indexing
- Read replicas for scaling

### Application
- Caching (Redis/ElastiCache)
- Async processing
- Load balancing
- Auto-scaling

### AWS Services
- CloudFront for static assets
- ElastiCache for session storage
- RDS read replicas
- Application Load Balancer

## Contributing

1. Create a feature branch
2. Make your changes
3. Write tests
4. Submit a pull request

## License

[Your License Here]

## Support

For issues and questions:
- Create an issue in the repository
- Contact the development team
- Check the [CLOUD_DEPLOYMENT_GUIDE.md](CLOUD_DEPLOYMENT_GUIDE.md)

## Changelog

### Version 0.0.1-SNAPSHOT (Cloud Ready)
- ✅ Removed desktop file chooser dependencies
- ✅ Externalized all configuration
- ✅ Added AWS S3 integration
- ✅ Implemented connection pooling
- ✅ Added Docker support
- ✅ Configured CloudWatch logging
- ✅ Added health checks and monitoring
- ✅ Implemented security best practices

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [AWS Elastic Beanstalk](https://docs.aws.amazon.com/elasticbeanstalk/)
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [Docker Documentation](https://docs.docker.com/)
- [Cloud Deployment Guide](CLOUD_DEPLOYMENT_GUIDE.md)
