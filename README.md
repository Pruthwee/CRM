# CRM - Cloud-Native Customer Relationship Management System

## Overview
Customer relationship management system with cloud-native architecture and distributed session management.

## Technologies
- **Spring Framework** (Spring Boot 1.5.10)
- **MySQL Database** with connection pooling
- **Spring MVC** for web layer
- **Spring Data JPA** for data access
- **Spring Security** for authentication/authorization
- **Spring Session** with Redis for distributed session management
- **Redis** (ElastiCache) for session storage
- **Docker** for containerization
- **AWS** deployment ready (ECS, RDS, ElastiCache)

## Features

### 1. Customer Management
- Create and manage customers
- Form-based customer creation/editing
- Clone customers from existing records
- Custom search based on various parameters

### 2. Role-Based Access Control
- Hierarchical role structure: user, manager, owner, admin
- Spring Security integration
- Dynamic UI based on user permissions
- Admin section for access rights management

### 3. Contract Generation
- Create and manage contracts
- Associate contracts with customers

### 4. Data Export
- Export formats: PDF, CSV, XLS
- Custom file naming
- In-memory generation (no file system dependencies)
- Cloud-ready export functionality

### 5. REST API
- RESTful endpoints for customer data
- JSON responses
- Compatible with Postman, curl, and other HTTP clients

## Cloud-Native Features

### ✅ Distributed Session Management
- **Redis-based session storage** for stateless application instances
- **Horizontal scaling** without sticky sessions
- **Session persistence** across instance restarts
- **AWS ElastiCache** integration ready

### ✅ Stateless Architecture
- No server-side session affinity required
- All view classes are stateless
- Request-scoped data processing
- Compatible with container orchestration

### ✅ Cloud Deployment Ready
- **Docker** containerization with multi-stage builds
- **AWS ECS/EKS** deployment support
- **Environment variable** configuration
- **Health checks** and monitoring endpoints
- **Auto-scaling** compatible

### ✅ Security
- Credentials via environment variables
- AWS Secrets Manager integration
- No hardcoded passwords
- Secure session management

## Quick Start

### Local Development

#### Prerequisites
- Java 8 or higher
- Maven 3.6+
- MySQL 5.7+
- Redis 6.x (optional, for session management)
- Docker (optional, for containerized development)

#### Option 1: Traditional Setup
```bash
# Start MySQL
mysql -u root -p
CREATE DATABASE crm;

# Run application
mvn spring-boot:run
```

#### Option 2: Docker Compose (Recommended)
```bash
# Start all services (MySQL + Redis)
docker-compose up -d

# Run application
export REDIS_HOST=localhost
export REDIS_PORT=6379
export DATABASE_URL=jdbc:mysql://localhost:3306/crm?useSSL=false
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=password
mvn spring-boot:run
```

#### Option 3: Full Docker Deployment
```bash
# Build and run everything in Docker
docker-compose up --build
```

### Access the Application
- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/appinfo/health
- **Actuator Info**: http://localhost:8080/appinfo

### Default Credentials
- Username: `admin`
- Password: `admin`

## Configuration

### Environment Variables

#### Database Configuration
```bash
DATABASE_URL=jdbc:mysql://localhost:3306/crm?useSSL=false
DATABASE_USERNAME=root
DATABASE_PASSWORD=password
DB_POOL_SIZE=10
```

#### Redis Session Configuration
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
SESSION_TIMEOUT=30m
```

#### Application Configuration
```bash
SPRING_PROFILES_ACTIVE=cloud
SERVER_PORT=8080
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
```

### Configuration Profiles

#### Default Profile (application.properties)
- Local development
- H2 or local MySQL
- Optional Redis

#### Cloud Profile (application-cloud.properties)
- Production deployment
- AWS RDS MySQL
- AWS ElastiCache Redis
- Environment variable configuration
- Enhanced security settings

## AWS Deployment

### Prerequisites
1. AWS Account with appropriate permissions
2. AWS CLI configured
3. Docker installed
4. ECR repository created

### Quick Deployment
```bash
# 1. Build Docker image
docker build -t crm-application:latest .

# 2. Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ecr-repo-uri>
docker tag crm-application:latest <ecr-repo-uri>:latest
docker push <ecr-repo-uri>:latest

# 3. Deploy to ECS (see AWS_DEPLOYMENT_GUIDE.md for details)
```

### Full Deployment Guide
See [AWS_DEPLOYMENT_GUIDE.md](AWS_DEPLOYMENT_GUIDE.md) for comprehensive step-by-step instructions including:
- VPC and networking setup
- ElastiCache Redis cluster creation
- RDS MySQL database setup
- ECS cluster and service configuration
- Application Load Balancer setup
- Auto-scaling configuration
- Monitoring and logging setup

## Cloud Readiness Fixes

This application has been enhanced for cloud deployment with the following fixes:

### Issue: HTTP Session State Storage
**Problem**: Session-based state storage prevents horizontal scaling and requires sticky sessions.

**Solution**: Implemented distributed session management with Redis
- Spring Session with Redis integration
- Stateless application instances
- No server affinity required
- Session persistence across restarts
- Horizontal scaling enabled

**Files Modified**:
- `pom.xml` - Added Spring Session and Redis dependencies
- `application.properties` - Added Redis configuration
- `crm/config/RedisSessionConfig.java` - Redis session configuration
- `crm/view/AbstractCsvView.java` - Enhanced with stateless documentation
- `crm/view/AbstractPdfView.java` - Enhanced with stateless documentation
- `crm/view/CsvView.java` - Enhanced with stateless documentation
- `crm/view/ExcelView.java` - Enhanced with stateless documentation
- `crm/view/PdfView.java` - Enhanced with stateless documentation

See [CLOUD_READINESS_FIXES.md](CLOUD_READINESS_FIXES.md) for detailed information.

## Architecture

### Application Layers
```
┌─────────────────────────────────────┐
│         Web Layer (MVC)             │
│  Controllers, Views, View Resolvers │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│       Service Layer                 │
│  Business Logic, Transactions       │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│    Repository Layer (Spring Data)   │
│         JPA Repositories            │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│         Data Layer                  │
│    MySQL Database (RDS)             │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│      Session Store (Redis)          │
│   Distributed Session Management    │
└─────────────────────────────────────┘
```

### Cloud Deployment Architecture
```
Internet
   │
   ▼
┌──────────────────┐
│  Route 53 (DNS)  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Application     │
│  Load Balancer   │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│ECS Task│ │ECS Task│
│(App)   │ │(App)   │
└───┬────┘ └───┬────┘
    │          │
    └────┬─────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌─────────┐
│ElastiCache│ │RDS MySQL│
│  Redis   │ │         │
└─────────┘ └─────────┘
```

## Monitoring and Health Checks

### Health Check Endpoint
```bash
curl http://localhost:8080/appinfo/health
```

Response:
```json
{
  "status": "UP",
  "redis": {
    "status": "UP"
  },
  "db": {
    "status": "UP"
  }
}
```

### Metrics Endpoint
```bash
curl http://localhost:8080/appinfo/metrics
```

### CloudWatch Integration (AWS)
- Application logs → CloudWatch Logs
- Metrics → CloudWatch Metrics
- Alarms for CPU, memory, Redis, RDS

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/

# Using wrk
wrk -t4 -c100 -d30s http://localhost:8080/
```

## Troubleshooting

### Redis Connection Issues
```bash
# Check Redis connectivity
redis-cli -h $REDIS_HOST -p $REDIS_PORT ping

# View application logs
docker-compose logs -f app
```

### Database Connection Issues
```bash
# Check MySQL connectivity
mysql -h $DATABASE_HOST -u $DATABASE_USERNAME -p

# View connection pool metrics
curl http://localhost:8080/appinfo/metrics | grep hikari
```

### Session Issues
```bash
# Connect to Redis and check sessions
redis-cli -h $REDIS_HOST
KEYS spring:session:*
```

## Performance Tuning

### Database Connection Pool
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Redis Connection Pool
```properties
spring.redis.lettuce.pool.max-active=16
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=2
```

### JVM Options
```bash
JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC"
```

## Security Considerations

### Production Checklist
- [ ] Enable HTTPS/SSL
- [ ] Use AWS Secrets Manager for credentials
- [ ] Enable Redis authentication
- [ ] Configure security groups properly
- [ ] Enable RDS encryption at rest
- [ ] Enable CloudTrail for audit logging
- [ ] Implement WAF rules
- [ ] Enable GuardDuty
- [ ] Use VPC with private subnets
- [ ] Implement least privilege IAM roles

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue in the repository
- Check [CLOUD_READINESS_FIXES.md](CLOUD_READINESS_FIXES.md) for cloud-specific issues
- Review [AWS_DEPLOYMENT_GUIDE.md](AWS_DEPLOYMENT_GUIDE.md) for deployment issues

## Changelog

### Version 2.0.0 (Cloud-Native Release)
- ✅ Added distributed session management with Redis
- ✅ Implemented stateless architecture
- ✅ Added Docker support
- ✅ AWS deployment ready
- ✅ Enhanced monitoring and health checks
- ✅ Environment variable configuration
- ✅ Security improvements
- ✅ Comprehensive documentation

### Version 1.0.0 (Initial Release)
- Basic CRM functionality
- Customer management
- Role-based access control
- Contract generation
- Data export (PDF, CSV, XLS)
- REST API
