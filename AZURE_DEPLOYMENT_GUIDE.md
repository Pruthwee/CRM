# Cloud Deployment Configuration Guide

## Azure Cloud Deployment Configuration

This application has been modernized for Azure cloud deployment with the following cloud-native patterns:

### 1. Azure Blob Storage Configuration

The application now uses Azure Blob Storage instead of local file system operations.

**Required Environment Variables:**

```bash
# Azure Storage Connection String (Required)
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=<your-account>;AccountKey=<your-key>;EndpointSuffix=core.windows.net

# Azure Storage Container Name (Optional, defaults to 'crm-files')
AZURE_STORAGE_CONTAINER_NAME=crm-files
```

**Azure Portal Setup:**
1. Create an Azure Storage Account
2. Navigate to "Access keys" and copy the connection string
3. Set the connection string as an environment variable or in application.properties

### 2. Database Configuration

The application uses environment variables for database configuration to support cloud deployments.

**Required Environment Variables:**

```bash
# Database URL (Azure SQL Database or MySQL)
DATABASE_URL=jdbc:mysql://<your-server>.mysql.database.azure.com:3306/crm?useSSL=true&requireSSL=true

# Database Credentials
DATABASE_USERNAME=<your-username>@<your-server>
DATABASE_PASSWORD=<your-password>

# Database DDL Mode (use 'update' for production)
DB_DDL_AUTO=update

# Connection Pool Configuration
DB_POOL_SIZE=10
DB_POOL_MIN_IDLE=2
DB_CONNECTION_TIMEOUT=30000
DB_IDLE_TIMEOUT=600000
DB_MAX_LIFETIME=1800000
```

**Azure Database for MySQL Setup:**
1. Create an Azure Database for MySQL server
2. Configure firewall rules to allow Azure services
3. Create the 'crm' database
4. Set the connection details as environment variables

### 3. Timezone Configuration

The application uses UTC timezone by default for consistency across distributed cloud environments.

**Environment Variable:**

```bash
# Application Timezone (defaults to UTC)
APP_TIMEZONE=UTC
```

### 4. Server Configuration

**Environment Variable:**

```bash
# Server Port (defaults to 8080)
PORT=8080
```

### 5. Logging Configuration

The application uses structured logging for cloud monitoring and log aggregation.

**Environment Variables:**

```bash
# Root Log Level
LOG_LEVEL=INFO

# Application Log Level
APP_LOG_LEVEL=DEBUG

# Azure SDK Log Level
AZURE_LOG_LEVEL=INFO
```

## Azure App Service Deployment

### Option 1: Deploy using Azure CLI

```bash
# Login to Azure
az login

# Create a resource group
az group create --name crm-rg --location eastus

# Create an App Service plan
az appservice plan create --name crm-plan --resource-group crm-rg --sku B1 --is-linux

# Create a web app
az webapp create --name crm-app --resource-group crm-rg --plan crm-plan --runtime "JAVA|11-java11"

# Configure environment variables
az webapp config appsettings set --name crm-app --resource-group crm-rg --settings \
  AZURE_STORAGE_CONNECTION_STRING="<your-connection-string>" \
  DATABASE_URL="<your-database-url>" \
  DATABASE_USERNAME="<your-username>" \
  DATABASE_PASSWORD="<your-password>" \
  DB_DDL_AUTO="update" \
  APP_TIMEZONE="UTC"

# Deploy the application
az webapp deploy --name crm-app --resource-group crm-rg --src-path target/crm-0.0.1-SNAPSHOT.jar --type jar
```

### Option 2: Deploy using Maven Plugin

Add to pom.xml:

```xml
<plugin>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-webapp-maven-plugin</artifactId>
    <version>2.5.0</version>
    <configuration>
        <resourceGroup>crm-rg</resourceGroup>
        <appName>crm-app</appName>
        <region>eastus</region>
        <pricingTier>B1</pricingTier>
        <runtime>
            <os>Linux</os>
            <javaVersion>Java 11</javaVersion>
            <webContainer>Java SE</webContainer>
        </runtime>
        <deployment>
            <resources>
                <resource>
                    <directory>${project.basedir}/target</directory>
                    <includes>
                        <include>*.jar</include>
                    </includes>
                </resource>
            </resources>
        </deployment>
    </configuration>
</plugin>
```

Deploy:

```bash
mvn clean package azure-webapp:deploy
```

## Azure Container Instances Deployment

### Create Dockerfile

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Deploy to Azure Container Instances

```bash
# Build and push to Azure Container Registry
az acr create --resource-group crm-rg --name crmacr --sku Basic
az acr login --name crmacr
docker build -t crmacr.azurecr.io/crm:latest .
docker push crmacr.azurecr.io/crm:latest

# Create container instance
az container create \
  --resource-group crm-rg \
  --name crm-container \
  --image crmacr.azurecr.io/crm:latest \
  --cpu 1 \
  --memory 1.5 \
  --registry-login-server crmacr.azurecr.io \
  --registry-username <username> \
  --registry-password <password> \
  --dns-name-label crm-app \
  --ports 8080 \
  --environment-variables \
    AZURE_STORAGE_CONNECTION_STRING="<your-connection-string>" \
    DATABASE_URL="<your-database-url>" \
    DATABASE_USERNAME="<your-username>" \
    DATABASE_PASSWORD="<your-password>" \
    DB_DDL_AUTO="update" \
    APP_TIMEZONE="UTC"
```

## Azure Kubernetes Service (AKS) Deployment

### Create Kubernetes Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: crm-deployment
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
        image: crmacr.azurecr.io/crm:latest
        ports:
        - containerPort: 8080
        env:
        - name: AZURE_STORAGE_CONNECTION_STRING
          valueFrom:
            secretKeyRef:
              name: crm-secrets
              key: storage-connection-string
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
        - name: DB_DDL_AUTO
          value: "update"
        - name: APP_TIMEZONE
          value: "UTC"
---
apiVersion: v1
kind: Service
metadata:
  name: crm-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: crm
```

## Cloud-Native Features Implemented

### 1. File System Independence
- ✅ Replaced local file operations with Azure Blob Storage
- ✅ Removed java.io.File dependencies
- ✅ Implemented cloud-native file storage patterns

### 2. Reactive/Non-Blocking Operations
- ✅ Implemented Project Reactor for asynchronous operations
- ✅ Replaced blocking I/O with reactive streams
- ✅ Improved throughput and resource utilization

### 3. Timezone Independence
- ✅ Replaced server-local timezone with UTC
- ✅ Implemented explicit Clock usage
- ✅ Added timezone configuration support

### 4. Configuration Externalization
- ✅ Replaced hardcoded values with environment variables
- ✅ Implemented 12-factor app configuration patterns
- ✅ Added connection pooling for database

### 5. Cloud Monitoring Ready
- ✅ Structured logging for log aggregation
- ✅ Configurable log levels
- ✅ Azure SDK integration logging

## Testing the Application

### Local Testing with Azure Storage Emulator

```bash
# Install Azurite (Azure Storage Emulator)
npm install -g azurite

# Start Azurite
azurite --silent --location /tmp/azurite --debug /tmp/azurite/debug.log

# Set connection string for local testing
export AZURE_STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;"

# Run the application
mvn spring-boot:run
```

### Local Testing with Azure Cloud Storage

```bash
# Set your Azure Storage connection string
export AZURE_STORAGE_CONNECTION_STRING="<your-connection-string>"

# Set database configuration
export DATABASE_URL="jdbc:mysql://localhost:3306/crm?useSSL=false"
export DATABASE_USERNAME="root"
export DATABASE_PASSWORD="password"

# Run the application
mvn spring-boot:run
```

## Troubleshooting

### Issue: Azure Storage connection fails

**Solution:** Verify the connection string is correctly set and the storage account is accessible.

```bash
# Test connection using Azure CLI
az storage account show --name <your-account> --resource-group <your-rg>
```

### Issue: Database connection fails

**Solution:** Check firewall rules and connection string format.

```bash
# Test database connection
mysql -h <your-server>.mysql.database.azure.com -u <username>@<server> -p
```

### Issue: Application fails to start

**Solution:** Check environment variables and logs.

```bash
# View application logs
az webapp log tail --name crm-app --resource-group crm-rg
```

## Security Best Practices

1. **Never commit secrets to source control**
2. **Use Azure Key Vault for sensitive configuration**
3. **Enable SSL/TLS for all connections**
4. **Use managed identities when possible**
5. **Implement proper network security groups**
6. **Enable Azure Monitor and Application Insights**
7. **Regularly update dependencies**
8. **Use Azure Security Center recommendations**

## Next Steps

1. Set up Azure Key Vault for secrets management
2. Configure Azure Application Insights for monitoring
3. Implement Azure Active Directory authentication
4. Set up CI/CD pipeline with Azure DevOps or GitHub Actions
5. Configure auto-scaling policies
6. Implement health checks and readiness probes
7. Set up backup and disaster recovery
