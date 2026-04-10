# Azure App Service Configuration
# This file provides guidance for deploying to Azure App Service

## Environment Variables to Configure in Azure App Service

### Database Configuration
# DATABASE_URL=jdbc:mysql://<azure-mysql-server>.mysql.database.azure.com:3306/crm?useSSL=true&requireSSL=true
# DATABASE_USERNAME=<admin-username>@<server-name>
# DATABASE_PASSWORD=<secure-password>
# DB_DDL_AUTO=update
# DB_POOL_SIZE=20
# DB_MIN_IDLE=5
# DB_CONNECTION_TIMEOUT=30000

### Azure Blob Storage Configuration
# AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=<account-name>;AccountKey=<account-key>;EndpointSuffix=core.windows.net
# AZURE_STORAGE_CONTAINER=app-files

### Application Configuration
# APP_TIMEZONE=UTC
# APP_NAME=crm-application
# LOG_LEVEL=INFO
# APP_LOG_LEVEL=INFO
# THYMELEAF_CACHE=true

### Server Configuration
# PORT=8080 (automatically set by Azure App Service)

## Azure App Service Deployment Steps

1. Package the application as an executable JAR:
   mvn clean package -DskipTests

2. Deploy to Azure App Service using Azure CLI:
   az webapp deploy --resource-group <resource-group> --name <app-name> --src-path target/crm-0.0.1-SNAPSHOT.jar --type jar

3. Configure environment variables in Azure Portal:
   - Navigate to App Service > Configuration > Application settings
   - Add all required environment variables listed above

4. Enable Application Insights for monitoring:
   - Navigate to App Service > Application Insights
   - Enable Application Insights integration

5. Configure Azure Database for MySQL:
   - Create Azure Database for MySQL server
   - Configure firewall rules to allow Azure services
   - Update DATABASE_URL, DATABASE_USERNAME, and DATABASE_PASSWORD

6. Configure Azure Blob Storage:
   - Create Azure Storage Account
   - Create blob container for file storage
   - Update AZURE_STORAGE_CONNECTION_STRING

## Docker Deployment (Alternative)

1. Create Dockerfile in project root:
   FROM openjdk:8-jre-alpine
   COPY target/crm-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java","-jar","/app.jar"]

2. Build and push to Azure Container Registry:
   az acr build --registry <registry-name> --image crm-app:latest .

3. Deploy to Azure Container Instances or Azure Kubernetes Service

## Health Check Endpoint
The application exposes actuator endpoints at /appinfo for health monitoring.
Configure Azure App Service health check to use: /appinfo/health
