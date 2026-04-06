# Cloud Deployment Guide - Azure

This document describes the cloud readiness improvements made to the CRM application and how to deploy it to Azure.

## Cloud Readiness Improvements

### 1. Configuration Management
- **Fixed**: Replaced hardcoded database credentials with environment variables
- **Fixed**: Externalized all configuration using Spring Boot properties with environment variable overrides
- **Fixed**: Added Azure-specific configuration profile (`application-azure.properties`)

### 2. File System Dependencies
- **Fixed**: Replaced local file system writes with Azure Blob Storage service
- **Fixed**: Removed Swing-based file chooser (ReadDataUtils) and replaced with classpath resource loading
- **Fixed**: Updated PdfController to use ByteArrayOutputStream and Azure Blob Storage instead of FileOutputStream
- **Fixed**: Refactored CSVTest to use InputStream and classpath resources instead of File objects

### 3. Packaging
- **Fixed**: Application uses JAR packaging with embedded Tomcat (cloud-native)
- **Fixed**: Configured Spring Boot Maven Plugin for executable JAR generation

### 4. Time/Clock Dependencies
- **Fixed**: DateTimeTestController now uses UTC timezone for consistency
- **Fixed**: Replaced legacy Date class with java.time API
- **Fixed**: Added configurable display timezone via environment variables

### 5. Database Connection Pooling
- **Fixed**: Added HikariCP configuration for cloud-optimized connection pooling
- **Fixed**: Configured connection timeouts and pool sizes via environment variables

## Environment Variables

The following environment variables should be configured in Azure:

### Required Variables
```bash
# Database Configuration
DATABASE_URL=jdbc:mysql://your-server.mysql.database.azure.com:3306/crm?useSSL=true
DATABASE_USERNAME=your-username@your-server
DATABASE_PASSWORD=your-password

# Azure Blob Storage (if enabled)
AZURE_STORAGE_ENABLED=true
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...
AZURE_STORAGE_CONTAINER=crm-files
```

### Optional Variables
```bash
# Database Pool Configuration
DB_POOL_SIZE=20
DB_POOL_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000

# Application Configuration
SERVER_PORT=8080
LOG_LEVEL=INFO
APP_LOG_LEVEL=INFO
DISPLAY_TIMEZONE=UTC

# Database DDL
DB_DDL_AUTO=update
```

## Azure Deployment Steps

### 1. Build the Application
```bash
mvn clean package -DskipTests
```

This creates an executable JAR: `target/crm-0.0.1-SNAPSHOT.jar`

### 2. Deploy to Azure App Service

#### Option A: Using Azure CLI
```bash
# Login to Azure
az login

# Create resource group
az group create --name crm-rg --location eastus

# Create App Service plan
az appservice plan create --name crm-plan --resource-group crm-rg --sku B1 --is-linux

# Create web app
az webapp create --name crm-app --resource-group crm-rg --plan crm-plan --runtime "JAVA|8-jre8"

# Configure environment variables
az webapp config appsettings set --name crm-app --resource-group crm-rg --settings \
  DATABASE_URL="jdbc:mysql://your-server.mysql.database.azure.com:3306/crm?useSSL=true" \
  DATABASE_USERNAME="your-username@your-server" \
  DATABASE_PASSWORD="your-password" \
  AZURE_STORAGE_ENABLED="true" \
  AZURE_STORAGE_CONNECTION_STRING="your-connection-string" \
  SPRING_PROFILES_ACTIVE="azure"

# Deploy JAR
az webapp deploy --name crm-app --resource-group crm-rg --src-path target/crm-0.0.1-SNAPSHOT.jar --type jar
```

#### Option B: Using Azure Portal
1. Navigate to Azure Portal
2. Create new App Service (Java 8, Linux)
3. Configure Application Settings with environment variables
4. Deploy JAR via Deployment Center

### 3. Configure Azure Database for MySQL
```bash
# Create MySQL server
az mysql server create --resource-group crm-rg --name crm-mysql-server \
  --location eastus --admin-user adminuser --admin-password YourPassword123! \
  --sku-name B_Gen5_1 --version 5.7

# Configure firewall to allow Azure services
az mysql server firewall-rule create --resource-group crm-rg \
  --server crm-mysql-server --name AllowAzureServices \
  --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0

# Create database
az mysql db create --resource-group crm-rg --server-name crm-mysql-server --name crm
```

### 4. Configure Azure Blob Storage
```bash
# Create storage account
az storage account create --name crmstorageaccount --resource-group crm-rg \
  --location eastus --sku Standard_LRS

# Create container
az storage container create --name crm-files --account-name crmstorageaccount

# Get connection string
az storage account show-connection-string --name crmstorageaccount --resource-group crm-rg
```

## Health Checks

The application exposes health check endpoints via Spring Boot Actuator:

- Health: `http://your-app.azurewebsites.net/appinfo/health`
- Info: `http://your-app.azurewebsites.net/appinfo/info`
- Metrics: `http://your-app.azurewebsites.net/appinfo/metrics`

## Monitoring

Configure Azure Application Insights for monitoring:

1. Create Application Insights resource in Azure Portal
2. Add instrumentation key to App Service configuration
3. Monitor logs, metrics, and performance

## Troubleshooting

### Application won't start
- Check environment variables are set correctly
- Verify database connection string
- Check application logs in Azure Portal

### File upload fails
- Verify Azure Blob Storage connection string
- Check AZURE_STORAGE_ENABLED is set to true
- Verify container exists and permissions are correct

### Database connection issues
- Verify firewall rules allow App Service IP
- Check connection string format
- Verify credentials are correct

## Local Development

For local development without Azure services:

```bash
# Run with default profile (uses local MySQL)
mvn spring-boot:run

# Or with environment variables
export DATABASE_URL=jdbc:mysql://localhost:3306/crm?useSSL=false
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=password
mvn spring-boot:run
```

## Security Considerations

1. **Never commit credentials**: Use environment variables or Azure Key Vault
2. **Enable SSL**: Always use SSL for database connections in production
3. **Secure storage**: Use Azure Blob Storage with proper access controls
4. **Network security**: Configure NSG rules and firewall appropriately
5. **Secrets management**: Consider using Azure Key Vault for sensitive data

## Next Steps

1. Enable Azure Application Insights for monitoring
2. Configure Azure Key Vault for secrets management
3. Set up CI/CD pipeline with Azure DevOps or GitHub Actions
4. Configure auto-scaling based on load
5. Implement backup and disaster recovery strategy
