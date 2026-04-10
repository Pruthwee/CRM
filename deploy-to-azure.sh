#!/bin/bash

###############################################################################
# Azure Deployment Script for CRM Application
# This script automates the deployment of the CRM application to Azure
###############################################################################

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration variables (set these before running)
RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-crm-rg}"
LOCATION="${AZURE_LOCATION:-eastus}"
APP_SERVICE_PLAN="${AZURE_APP_SERVICE_PLAN:-crm-plan}"
APP_NAME="${AZURE_APP_NAME:-crm-app-$(date +%s)}"
STORAGE_ACCOUNT="${AZURE_STORAGE_ACCOUNT:-crmstorage$(date +%s)}"
MYSQL_SERVER="${AZURE_MYSQL_SERVER:-crm-mysql-$(date +%s)}"
MYSQL_ADMIN_USER="${AZURE_MYSQL_ADMIN_USER:-crmadmin}"
MYSQL_ADMIN_PASSWORD="${AZURE_MYSQL_ADMIN_PASSWORD}"
MYSQL_DATABASE="${AZURE_MYSQL_DATABASE:-crm}"

# Functions
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check if Azure CLI is installed
    if ! command -v az &> /dev/null; then
        print_error "Azure CLI is not installed. Please install it first."
        exit 1
    fi
    
    # Check if logged in to Azure
    if ! az account show &> /dev/null; then
        print_error "Not logged in to Azure. Please run 'az login' first."
        exit 1
    fi
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install it first."
        exit 1
    fi
    
    print_info "Prerequisites check passed."
}

create_resource_group() {
    print_info "Creating resource group: $RESOURCE_GROUP"
    az group create \
        --name "$RESOURCE_GROUP" \
        --location "$LOCATION" \
        --output none
    print_info "Resource group created successfully."
}

create_storage_account() {
    print_info "Creating storage account: $STORAGE_ACCOUNT"
    az storage account create \
        --name "$STORAGE_ACCOUNT" \
        --resource-group "$RESOURCE_GROUP" \
        --location "$LOCATION" \
        --sku Standard_LRS \
        --kind StorageV2 \
        --output none
    
    # Get connection string
    STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
        --name "$STORAGE_ACCOUNT" \
        --resource-group "$RESOURCE_GROUP" \
        --query connectionString \
        --output tsv)
    
    # Create container
    az storage container create \
        --name "crm-files" \
        --connection-string "$STORAGE_CONNECTION_STRING" \
        --output none
    
    print_info "Storage account created successfully."
}

create_mysql_server() {
    print_info "Creating MySQL server: $MYSQL_SERVER"
    
    if [ -z "$MYSQL_ADMIN_PASSWORD" ]; then
        print_error "MySQL admin password not set. Please set AZURE_MYSQL_ADMIN_PASSWORD environment variable."
        exit 1
    fi
    
    az mysql server create \
        --resource-group "$RESOURCE_GROUP" \
        --name "$MYSQL_SERVER" \
        --location "$LOCATION" \
        --admin-user "$MYSQL_ADMIN_USER" \
        --admin-password "$MYSQL_ADMIN_PASSWORD" \
        --sku-name B_Gen5_1 \
        --ssl-enforcement Enabled \
        --version 5.7 \
        --output none
    
    # Create database
    az mysql db create \
        --resource-group "$RESOURCE_GROUP" \
        --server-name "$MYSQL_SERVER" \
        --name "$MYSQL_DATABASE" \
        --output none
    
    # Configure firewall to allow Azure services
    az mysql server firewall-rule create \
        --resource-group "$RESOURCE_GROUP" \
        --server-name "$MYSQL_SERVER" \
        --name AllowAzureServices \
        --start-ip-address 0.0.0.0 \
        --end-ip-address 0.0.0.0 \
        --output none
    
    print_info "MySQL server created successfully."
}

create_app_service() {
    print_info "Creating App Service Plan: $APP_SERVICE_PLAN"
    az appservice plan create \
        --name "$APP_SERVICE_PLAN" \
        --resource-group "$RESOURCE_GROUP" \
        --location "$LOCATION" \
        --sku B1 \
        --is-linux \
        --output none
    
    print_info "Creating Web App: $APP_NAME"
    az webapp create \
        --resource-group "$RESOURCE_GROUP" \
        --plan "$APP_SERVICE_PLAN" \
        --name "$APP_NAME" \
        --runtime "JAVA|8-jre8" \
        --output none
    
    print_info "App Service created successfully."
}

configure_app_settings() {
    print_info "Configuring application settings..."
    
    DATABASE_URL="jdbc:mysql://${MYSQL_SERVER}.mysql.database.azure.com:3306/${MYSQL_DATABASE}?useSSL=true&requireSSL=true"
    
    az webapp config appsettings set \
        --resource-group "$RESOURCE_GROUP" \
        --name "$APP_NAME" \
        --settings \
            DATABASE_URL="$DATABASE_URL" \
            DATABASE_USERNAME="${MYSQL_ADMIN_USER}@${MYSQL_SERVER}" \
            DATABASE_PASSWORD="$MYSQL_ADMIN_PASSWORD" \
            AZURE_STORAGE_CONNECTION_STRING="$STORAGE_CONNECTION_STRING" \
            AZURE_STORAGE_CONTAINER_NAME="crm-files" \
            DB_DDL_AUTO="update" \
            LOG_LEVEL_ROOT="INFO" \
            LOG_LEVEL_APP="DEBUG" \
            THYMELEAF_CACHE="true" \
        --output none
    
    print_info "Application settings configured successfully."
}

build_and_deploy() {
    print_info "Building application..."
    mvn clean package -DskipTests
    
    print_info "Deploying application to Azure..."
    az webapp deploy \
        --resource-group "$RESOURCE_GROUP" \
        --name "$APP_NAME" \
        --src-path target/crm-0.0.1-SNAPSHOT.jar \
        --type jar \
        --output none
    
    print_info "Application deployed successfully."
}

print_summary() {
    echo ""
    echo "=========================================="
    echo "Deployment Summary"
    echo "=========================================="
    echo "Resource Group: $RESOURCE_GROUP"
    echo "Location: $LOCATION"
    echo "App Service: $APP_NAME"
    echo "Storage Account: $STORAGE_ACCOUNT"
    echo "MySQL Server: $MYSQL_SERVER"
    echo "Application URL: https://${APP_NAME}.azurewebsites.net"
    echo "Health Check: https://${APP_NAME}.azurewebsites.net/appinfo/health"
    echo "=========================================="
}

# Main execution
main() {
    print_info "Starting Azure deployment for CRM Application..."
    
    check_prerequisites
    create_resource_group
    create_storage_account
    create_mysql_server
    create_app_service
    configure_app_settings
    build_and_deploy
    print_summary
    
    print_info "Deployment completed successfully!"
}

# Run main function
main
