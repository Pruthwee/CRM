# Cloud Readiness Improvements

This document describes the cloud readiness improvements made to the CRM application for Azure deployment.

## Summary of Changes

### 1. File System Dependencies Removed
- **ReadDataUtils.java**: Replaced Swing JFileChooser with Azure Blob Storage integration
- **PdfController.java**: Replaced local file writes with Azure Blob Storage uploads
- **CSVTest.java**: Replaced static file initialization with Spring-managed service using Azure Blob Storage

### 2. Configuration Management
- **application.properties**: All hardcoded values replaced with environment variables
  - Database credentials externalized
  - Azure Storage connection strings configurable
  - Server port configurable
  - Timezone configurable
  - Connection pool settings configurable

### 3. Time/Clock Dependencies Fixed
- **DateTimeTestController.java**: Replaced local time dependencies with UTC-based, timezone-aware handling
  - Uses Clock.systemUTC() for consistent behavior across distributed environments
  - Timezone configurable via environment variable (APP_TIMEZONE)
  - Returns timezone-aware ZonedDateTime objects

### 4. WAR to JAR Packaging
- Application already configured as JAR packaging in pom.xml
- Compatible with embedded Tomcat server
- All servlet-based views updated with cloud-ready documentation
- Ready for Azure App Service deployment

### 5. Static Initializer Issues Fixed
- **CSVTest.java**: Removed static initializer with I/O operations
  - Converted to Spring-managed service with @PostConstruct
  - Uses lazy initialization to avoid startup failures

### 6. Azure Integration
- Added Azure Blob Storage SDK dependencies
- Created AzureStorageConfig for centralized Azure configuration
- All file operations now support Azure Blob Storage
- Graceful fallback for local development without Azure Storage

## Cloud-Native Features

### 12-Factor App Compliance
1. **Codebase**: Single codebase tracked in version control
2. **Dependencies**: All dependencies explicitly declared in pom.xml
3. **Config**: Configuration externalized via environment variables
4. **Backing Services**: Database and storage treated as attached resources
5. **Build, Release, Run**: Strict separation via Maven build process
6. **Processes**: Application is stateless (no local file dependencies)
7. **Port Binding**: Server port configurable via environment variable
8. **Concurrency**: Horizontally scalable with stateless design
9. **Disposability**: Fast startup and graceful shutdown
10. **Dev/Prod Parity**: Same configuration mechanism for all environments
11. **Logs**: Console-based logging for cloud log aggregation
12. **Admin Processes**: Actuator endpoints for management tasks

### Azure-Specific Features
- Azure Blob Storage integration for file persistence
- Azure Database for MySQL support
- Environment variable-based configuration
- Connection pooling with HikariCP
- Health check endpoints via Spring Actuator
- Timezone-aware date/time handling

## Environment Variables

### Required for Production
```bash
# Database
DATABASE_URL=jdbc:mysql://<server>.mysql.database.azure.com:3306/crm
DATABASE_USERNAME=<username>
DATABASE_PASSWORD=<password>

# Azure Storage
AZURE_STORAGE_CONNECTION_STRING=<connection-string>
AZURE_STORAGE_CONTAINER=app-files
```

### Optional Configuration
```bash
# Database Pool
DB_POOL_SIZE=20
DB_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000

# Application
APP_TIMEZONE=UTC
APP_NAME=crm-application
LOG_LEVEL=INFO
THYMELEAF_CACHE=true
```

## Deployment

### Azure App Service
1. Build: `mvn clean package`
2. Deploy: `az webapp deploy --resource-group <rg> --name <app> --src-path target/crm-0.0.1-SNAPSHOT.jar`
3. Configure environment variables in Azure Portal

### Docker
1. Build: `docker build -t crm-app .`
2. Run: `docker run -p 8080:8080 -e DATABASE_URL=... crm-app`

See AZURE_DEPLOYMENT.md for detailed deployment instructions.

## Testing

### Local Development
The application can run locally without Azure Storage configured. File operations will fail gracefully with appropriate error messages.

### Cloud Testing
1. Configure Azure Storage connection string
2. Configure Azure Database for MySQL
3. Set all required environment variables
4. Deploy to Azure App Service or Container

## Monitoring

- Health check: `/appinfo/health`
- Metrics: `/appinfo/metrics`
- Application info: `/appinfo/info`

## Security Considerations

1. Never commit credentials to version control
2. Use Azure Key Vault for sensitive configuration
3. Enable SSL/TLS for database connections
4. Use managed identities for Azure resource access
5. Implement proper authentication and authorization

## Performance Optimizations

1. Connection pooling configured with HikariCP
2. Thymeleaf caching enabled in production
3. Stateless design for horizontal scaling
4. Efficient Azure Blob Storage operations

## Troubleshooting

### Common Issues
1. **Azure Storage not configured**: Set AZURE_STORAGE_CONNECTION_STRING
2. **Database connection fails**: Check DATABASE_URL and firewall rules
3. **Timezone issues**: Set APP_TIMEZONE environment variable
4. **Port conflicts**: Set PORT environment variable

### Logs
Check application logs in Azure Portal under:
- App Service > Log stream
- Application Insights > Logs
