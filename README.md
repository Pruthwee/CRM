# CRM Application - Cloud-Ready Version

## Overview
This CRM (Customer Relationship Management) application has been modernized for Azure cloud deployment with cloud-native patterns and best practices.

## Cloud Readiness Improvements

### ✅ Fixed Issues

1. **Hard-coded File Paths** → **Azure Blob Storage**
   - Replaced local file system operations with Azure Blob Storage SDK
   - All file operations now use cloud storage
   - Files persist across container restarts and scaling events

2. **Local File System Writes** → **Azure Blob Storage**
   - PDF generation now stores files in Azure Blob Storage
   - No data loss during container restarts
   - Scalable and durable storage

3. **Java.io.File Usage** → **Azure Blob Storage**
   - CSV processing migrated to Azure Blob Storage
   - Removed local file system dependencies
   - Cloud-native file handling

4. **Clock/Time Dependencies** → **UTC Timezone**
   - All timestamps use UTC for consistency
   - Timezone-agnostic operations
   - Compatible with distributed cloud environments

5. **Synchronous Blocking Operations** → **Reactive Patterns**
   - Implemented Project Reactor for async operations
   - Non-blocking I/O for better throughput
   - Improved resource utilization

6. **Hard-coded Configuration** → **Environment Variables**
   - All configuration externalized
   - Database credentials via environment variables
   - Azure Storage configuration via environment variables

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Azure Cloud Platform                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐      ┌──────────────┐      ┌───────────┐ │
│  │  Azure App   │      │   Azure      │      │  Azure    │ │
│  │   Service    │─────▶│  Database    │      │   Blob    │ │
│  │  (CRM App)   │      │  for MySQL   │      │  Storage  │ │
│  └──────────────┘      └──────────────┘      └───────────┘ │
│         │                                            ▲       │
│         │                                            │       │
│         └────────────────────────────────────────────┘       │
│                    (File Operations)                         │
└─────────────────────────────────────────────────────────────┘
```

## Prerequisites

- Java 8 or higher
- Maven 3.6+
- Azure CLI (for deployment)
- Azure subscription

## Local Development

### 1. Clone the repository
```bash
git clone <repository-url>
cd CRM_Component
```

### 2. Set environment variables
```bash
export DATABASE_URL="jdbc:mysql://localhost:3306/crm?useSSL=false"
export DATABASE_USERNAME="root"
export DATABASE_PASSWORD="password"
export AZURE_STORAGE_CONNECTION_STRING="<your-connection-string>"
export AZURE_STORAGE_CONTAINER_NAME="crm-files"
```

### 3. Build the application
```bash
mvn clean package
```

### 4. Run the application
```bash
java -jar target/crm-0.0.1-SNAPSHOT.jar
```

### 5. Access the application
- Application: http://localhost:8080
- Health Check: http://localhost:8080/appinfo/health
- Metrics: http://localhost:8080/appinfo/metrics

## Azure Deployment

### Quick Deployment (Automated)

1. **Set required environment variables**
   ```bash
   export AZURE_RESOURCE_GROUP="crm-rg"
   export AZURE_LOCATION="eastus"
   export AZURE_APP_NAME="my-crm-app"
   export AZURE_MYSQL_ADMIN_PASSWORD="YourSecurePassword123!"
   ```

2. **Run deployment script**
   ```bash
   ./deploy-to-azure.sh
   ```

### Manual Deployment

See [AZURE_DEPLOYMENT_GUIDE.md](AZURE_DEPLOYMENT_GUIDE.md) for detailed manual deployment instructions.

## Docker Deployment

### Build Docker image
```bash
docker build -t crm-app:latest .
```

### Run Docker container
```bash
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL="jdbc:mysql://host.docker.internal:3306/crm" \
  -e DATABASE_USERNAME="root" \
  -e DATABASE_PASSWORD="password" \
  -e AZURE_STORAGE_CONNECTION_STRING="<connection-string>" \
  crm-app:latest
```

## Configuration

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | JDBC connection string | `jdbc:mysql://server:3306/crm` |
| `DATABASE_USERNAME` | Database username | `crmadmin@server` |
| `DATABASE_PASSWORD` | Database password | `SecurePassword123!` |
| `AZURE_STORAGE_CONNECTION_STRING` | Azure Storage connection | `DefaultEndpointsProtocol=https;...` |
| `AZURE_STORAGE_CONTAINER_NAME` | Blob container name | `crm-files` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Application port | `8080` |
| `DB_DDL_AUTO` | Hibernate DDL mode | `update` |
| `DB_POOL_SIZE` | Connection pool size | `10` |
| `LOG_LEVEL_ROOT` | Root log level | `INFO` |
| `LOG_LEVEL_APP` | App log level | `DEBUG` |
| `THYMELEAF_CACHE` | Template caching | `false` |

## Features

- Customer Management (CRUD operations)
- Contract Management
- User Authentication & Authorization
- PDF Generation (stored in Azure Blob Storage)
- CSV Import/Export (from/to Azure Blob Storage)
- Excel Export
- Role-based Access Control
- Health Monitoring
- Metrics Collection

## API Endpoints

### Application Endpoints
- `GET /` - Home page
- `GET /customers` - List customers
- `GET /contracts` - List contracts
- `GET /users` - List users
- `POST /pdf-generator` - Generate PDF
- `GET /date/test` - Date/time test

### Management Endpoints
- `GET /appinfo/health` - Health check
- `GET /appinfo/info` - Application info
- `GET /appinfo/metrics` - Application metrics

## Cloud-Native Features

### 12-Factor App Compliance
- ✅ Codebase: Single codebase tracked in version control
- ✅ Dependencies: Explicitly declared via Maven
- ✅ Config: Externalized via environment variables
- ✅ Backing Services: Attached resources (database, storage)
- ✅ Build, Release, Run: Separate stages
- ✅ Processes: Stateless processes
- ✅ Port Binding: Self-contained with embedded server
- ✅ Concurrency: Horizontal scaling ready
- ✅ Disposability: Fast startup and graceful shutdown
- ✅ Dev/Prod Parity: Same configuration mechanism
- ✅ Logs: Treat logs as event streams
- ✅ Admin Processes: Run as one-off processes

### Resilience Patterns
- Connection pooling with HikariCP
- Configurable timeouts
- Health checks
- Graceful degradation (works without Azure Storage)

### Security
- No hard-coded credentials
- SSL/TLS for database connections
- Secure Azure Blob Storage access
- Non-root Docker user

## Monitoring

### Health Checks
```bash
curl http://localhost:8080/appinfo/health
```

### Metrics
```bash
curl http://localhost:8080/appinfo/metrics
```

## Troubleshooting

### Application won't start
- Check environment variables are set correctly
- Verify database connectivity
- Check Azure Storage connection string

### Database connection errors
- Verify MySQL server is running
- Check firewall rules
- Ensure SSL is properly configured

### File upload/download issues
- Verify Azure Storage connection string
- Check container exists
- Verify storage account access keys

## Performance Tuning

### JVM Options
```bash
JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Database Connection Pool
```properties
DB_POOL_SIZE=20
DB_MIN_IDLE=10
DB_CONNECTION_TIMEOUT=30000
```

## Support

For issues and questions:
1. Check the [AZURE_DEPLOYMENT_GUIDE.md](AZURE_DEPLOYMENT_GUIDE.md)
2. Review application logs
3. Check Azure Portal for resource status

## License

[Your License Here]

## Contributors

[Your Team/Contributors Here]
