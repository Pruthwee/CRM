# Cloud Deployment Guide for CRM Application

## Overview
This CRM application has been modernized for cloud-native deployment on Azure. All cloud readiness issues have been addressed to ensure successful deployment in containerized and cloud environments.

## Cloud Readiness Fixes Applied

### 1. File System Dependencies Removed
- **Issue**: Local file system writes for PDF generation
- **Fix**: PDFs are now generated in-memory and can be stored in Azure Blob Storage
- **Configuration**: Set `azure.storage.enabled=true` and configure connection string

### 2. Static Initializers Refactored
- **Issue**: I/O operations in static blocks causing startup failures
- **Fix**: Moved to Spring `@PostConstruct` with proper error handling
- **Configuration**: Enable CSV processing with `csv.processing.enabled=true`

### 3. Timezone Standardization
- **Issue**: Local timezone dependencies causing inconsistencies
- **Fix**: All timestamps use UTC internally, timezone configurable for display
- **Configuration**: Set `app.timezone.display` for user-facing timezone

### 4. JAR Packaging
- **Issue**: WAR packaging incompatible with cloud containers
- **Fix**: Changed to executable JAR with embedded Tomcat
- **Build**: `mvn clean package` produces cloud-ready JAR

### 5. Externalized Configuration
- **Issue**: Hardcoded database credentials and settings
- **Fix**: All configuration uses environment variables
- **Configuration**: See Environment Variables section below

## Environment Variables

### Required for Production
```bash
# Database Configuration
DATABASE_URL=jdbc:mysql://your-azure-mysql.mysql.database.azure.com:3306/crm?useSSL=true
DATABASE_USERNAME=your-db-username
DATABASE_PASSWORD=your-db-password

# Server Configuration
PORT=8080
```

### Optional Configuration
```bash
# Database Connection Pool
DB_POOL_SIZE=20
DB_POOL_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000

# Azure Blob Storage (for PDF storage)
AZURE_STORAGE_ENABLED=true
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...
AZURE_BLOB_CONTAINER=pdf-documents

# Application Settings
APP_TIMEZONE=UTC
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
THYMELEAF_CACHE=true

# CSV Processing
CSV_PROCESSING_ENABLED=false
```

## Azure Deployment Options

### Option 1: Azure App Service
```bash
# Build the application
mvn clean package

# Deploy to Azure App Service
az webapp deploy --resource-group <resource-group> \
  --name <app-name> \
  --src-path target/crm-0.0.1-SNAPSHOT.jar \
  --type jar
```

### Option 2: Azure Container Instances
```dockerfile
# Dockerfile
FROM openjdk:8-jre-alpine
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build and push container
docker build -t crm-app:latest .
docker tag crm-app:latest <your-registry>.azurecr.io/crm-app:latest
docker push <your-registry>.azurecr.io/crm-app:latest

# Deploy to Azure Container Instances
az container create --resource-group <resource-group> \
  --name crm-app \
  --image <your-registry>.azurecr.io/crm-app:latest \
  --cpu 1 --memory 1.5 \
  --environment-variables DATABASE_URL=... DATABASE_USERNAME=... DATABASE_PASSWORD=...
```

### Option 3: Azure Kubernetes Service (AKS)
```yaml
# kubernetes-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: crm-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: crm
  template:
    metadata:
      labels:
        app: crm
    spec:
      containers:
      - name: crm
        image: <your-registry>.azurecr.io/crm-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: crm-secrets
              key: database-url
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: crm-secrets
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: crm-secrets
              key: database-password
```

## Azure Blob Storage Integration

To enable Azure Blob Storage for PDF file storage:

1. **Add Azure SDK dependencies** in `pom.xml` (currently commented out):
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.20.0</version>
</dependency>
```

2. **Configure environment variables**:
```bash
AZURE_STORAGE_ENABLED=true
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...
AZURE_BLOB_CONTAINER=pdf-documents
```

3. **Uncomment Azure integration code** in:
   - `PdfController.java`
   - `AzureBlobStorageService.java`

## Database Configuration

### Azure Database for MySQL
```bash
# Create Azure MySQL server
az mysql server create --resource-group <resource-group> \
  --name <server-name> \
  --location eastus \
  --admin-user <admin-username> \
  --admin-password <admin-password> \
  --sku-name GP_Gen5_2

# Configure firewall
az mysql server firewall-rule create --resource-group <resource-group> \
  --server <server-name> \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

# Connection string
DATABASE_URL=jdbc:mysql://<server-name>.mysql.database.azure.com:3306/crm?useSSL=true&requireSSL=true
```

## Monitoring and Logging

### Application Insights (Recommended)
Add Application Insights for comprehensive monitoring:

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>applicationinsights-spring-boot-starter</artifactId>
    <version>2.6.4</version>
</dependency>
```

Configure with environment variable:
```bash
APPINSIGHTS_INSTRUMENTATIONKEY=your-instrumentation-key
```

### Health Checks
The application exposes health endpoints at:
- `/appinfo/health` - Application health status
- `/appinfo/info` - Application information
- `/appinfo/metrics` - Application metrics

## Build and Run

### Local Development
```bash
# Build
mvn clean package

# Run with default configuration
java -jar target/crm-0.0.1-SNAPSHOT.jar

# Run with custom configuration
java -jar target/crm-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/crm \
  --spring.datasource.username=root \
  --spring.datasource.password=password
```

### Production Build
```bash
# Build with production profile
mvn clean package -DskipTests

# The resulting JAR is cloud-ready and can be deployed to any Azure service
```

## Security Considerations

1. **Never commit secrets** - Use Azure Key Vault or environment variables
2. **Enable SSL/TLS** - Configure `useSSL=true` for database connections
3. **Use managed identities** - Leverage Azure Managed Identity for service authentication
4. **Enable HTTPS** - Configure SSL certificates in Azure App Service
5. **Implement rate limiting** - Use Azure API Management or Application Gateway

## Troubleshooting

### Common Issues

1. **Database connection timeout**
   - Increase `DB_CONNECTION_TIMEOUT` environment variable
   - Check Azure MySQL firewall rules
   - Verify connection string format

2. **Out of memory errors**
   - Increase container memory allocation
   - Tune JVM heap size: `-Xmx512m -Xms256m`

3. **PDF generation fails**
   - Check `PDF_STORAGE_PATH` is writable (for local storage)
   - Verify Azure Blob Storage configuration (for cloud storage)

4. **Timezone issues**
   - Ensure `APP_TIMEZONE` is set correctly
   - Verify database timezone settings

## Support

For issues or questions:
- Check application logs in Azure Portal
- Review health endpoints for diagnostics
- Enable DEBUG logging: `LOG_LEVEL_APP=DEBUG`
