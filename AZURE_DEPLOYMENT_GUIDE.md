# Azure Cloud Deployment Configuration Guide

## Overview
This application has been modernized for Azure cloud deployment with the following cloud-native patterns:

## Cloud Readiness Fixes Applied

### 1. File System Dependencies → Azure Blob Storage
**Issue**: Hard-coded file paths and local file system operations
**Fix**: Migrated to Azure Blob Storage SDK
- `ReadDataUtils.java`: Now uses Azure Blob Storage for file operations
- `PdfController.java`: Generates PDFs and stores them in Azure Blob Storage
- `CSVTest.java`: Reads CSV files from Azure Blob Storage

### 2. Synchronous Blocking Operations → Reactive Patterns
**Issue**: Blocking I/O operations reducing throughput
**Fix**: Implemented Project Reactor for async operations
- Added reactive methods using Mono/Flux
- Implemented CompletableFuture for async processing
- Non-blocking Azure SDK clients

### 3. Clock/Time Dependencies → UTC Timezone
**Issue**: Server-local timezone dependencies
**Fix**: All timestamps use UTC
- `DateTimeTestController.java`: Uses UTC Clock for consistency
- Application properties configured for UTC timezone
- ISO-8601 timestamp formats for API compatibility

### 4. Configuration Management → Environment Variables
**Issue**: Hard-coded configuration values
**Fix**: Externalized all configuration
- Database credentials via environment variables
- Azure Storage configuration via environment variables
- Port configuration externalized
- Connection pool settings configurable

## Required Environment Variables

### Database Configuration
```bash
DATABASE_URL=jdbc:mysql://<azure-mysql-server>.mysql.database.azure.com:3306/crm?useSSL=true
DATABASE_USERNAME=<your-username>@<server-name>
DATABASE_PASSWORD=<your-password>
DB_DDL_AUTO=update
DB_POOL_SIZE=10
DB_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000
```

### Azure Blob Storage Configuration
```bash
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=<account-name>;AccountKey=<account-key>;EndpointSuffix=core.windows.net
AZURE_STORAGE_CONTAINER_NAME=crm-files
AZURE_STORAGE_ACCOUNT_NAME=<your-storage-account>
AZURE_STORAGE_ACCOUNT_KEY=<your-storage-key>
```

### Application Configuration
```bash
PORT=8080
APP_NAME=crm-application
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
THYMELEAF_CACHE=true
```

## Azure Resources Required

### 1. Azure App Service or Container Instance
- Runtime: Java 8 or higher
- OS: Linux
- Pricing Tier: B1 or higher recommended

### 2. Azure Database for MySQL
- Version: 5.7 or 8.0
- Pricing Tier: Basic or General Purpose
- SSL Enforcement: Enabled
- Firewall: Allow Azure services

### 3. Azure Blob Storage
- Account Kind: StorageV2
- Performance: Standard
- Replication: LRS or GRS
- Container: crm-files (auto-created if not exists)

## Deployment Steps

### Option 1: Azure App Service

1. **Create App Service**
   ```bash
   az webapp create \
     --resource-group <resource-group> \
     --plan <app-service-plan> \
     --name <app-name> \
     --runtime "JAVA|8-jre8"
   ```

2. **Configure Environment Variables**
   ```bash
   az webapp config appsettings set \
     --resource-group <resource-group> \
     --name <app-name> \
     --settings \
       DATABASE_URL="<connection-string>" \
       DATABASE_USERNAME="<username>" \
       DATABASE_PASSWORD="<password>" \
       AZURE_STORAGE_CONNECTION_STRING="<connection-string>"
   ```

3. **Deploy Application**
   ```bash
   mvn clean package
   az webapp deploy \
     --resource-group <resource-group> \
     --name <app-name> \
     --src-path target/crm-0.0.1-SNAPSHOT.jar \
     --type jar
   ```

### Option 2: Azure Container Instances

1. **Build Docker Image**
   ```dockerfile
   FROM openjdk:8-jre-alpine
   COPY target/crm-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```

2. **Push to Azure Container Registry**
   ```bash
   az acr build \
     --registry <registry-name> \
     --image crm-app:latest .
   ```

3. **Deploy Container**
   ```bash
   az container create \
     --resource-group <resource-group> \
     --name crm-container \
     --image <registry-name>.azurecr.io/crm-app:latest \
     --environment-variables \
       DATABASE_URL="<connection-string>" \
       AZURE_STORAGE_CONNECTION_STRING="<connection-string>"
   ```

## Health Checks

The application exposes health check endpoints:
- `/appinfo/health` - Application health status
- `/appinfo/info` - Application information
- `/appinfo/metrics` - Application metrics

## Monitoring and Logging

### Application Insights Integration (Optional)
Add to pom.xml:
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>applicationinsights-spring-boot-starter</artifactId>
    <version>2.6.4</version>
</dependency>
```

Add to application.properties:
```properties
azure.application-insights.instrumentation-key=${APPINSIGHTS_INSTRUMENTATIONKEY}
```

## Security Considerations

1. **Use Azure Key Vault for Secrets**
   - Store database passwords in Key Vault
   - Store storage account keys in Key Vault
   - Reference secrets via environment variables

2. **Enable SSL/TLS**
   - Use HTTPS for all endpoints
   - Enable SSL for database connections
   - Use secure storage account connections

3. **Network Security**
   - Configure Azure Virtual Network
   - Use Private Endpoints for database
   - Enable Azure DDoS Protection

## Performance Optimization

1. **Connection Pooling**
   - HikariCP configured with optimal settings
   - Connection timeout: 30 seconds
   - Max pool size: 10 connections

2. **Caching**
   - Consider Azure Cache for Redis
   - Enable Thymeleaf template caching in production

3. **CDN**
   - Use Azure CDN for static assets
   - Enable compression

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
   - Verify firewall rules allow Azure services
   - Check connection string format
   - Ensure SSL is enabled

2. **Blob Storage Access Denied**
   - Verify connection string is correct
   - Check storage account access keys
   - Ensure container exists and has proper permissions

3. **Application Won't Start**
   - Check environment variables are set
   - Review application logs in Azure Portal
   - Verify Java version compatibility

## Cost Optimization

1. **Use appropriate pricing tiers**
   - Start with Basic tier for development
   - Scale to Standard/Premium for production

2. **Enable auto-scaling**
   - Configure based on CPU/memory metrics
   - Set minimum and maximum instances

3. **Use Azure Reserved Instances**
   - Save up to 72% with 1-year or 3-year commitments

## Support and Maintenance

- Monitor application health via Azure Portal
- Set up alerts for critical metrics
- Review logs regularly via Application Insights
- Keep dependencies updated for security patches
