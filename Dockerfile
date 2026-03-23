# Multi-stage Dockerfile for Cloud-Ready CRM Application
# Optimized for AWS ECS, Azure Container Instances, and GCP Cloud Run

# ============================================================================
# Stage 1: Build Stage
# ============================================================================
FROM maven:3.6.3-jdk-8-slim AS builder

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# ============================================================================
# Stage 2: Runtime Stage
# ============================================================================
FROM openjdk:8-jre-alpine

# Add metadata
LABEL maintainer="CRM Team"
LABEL description="Cloud-Ready CRM Application with Distributed Session Management"
LABEL version="1.0.0"

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/appinfo/health || exit 1

# Environment variables with defaults
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=production

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

# ============================================================================
# Build Instructions:
# ============================================================================
# docker build -t crm-app:latest .
#
# Run locally:
# docker run -p 8080:8080 \
#   -e DATABASE_URL=jdbc:mysql://host.docker.internal:3306/crm \
#   -e DATABASE_USERNAME=root \
#   -e DATABASE_PASSWORD=password \
#   -e REDIS_HOST=host.docker.internal \
#   -e REDIS_PORT=6379 \
#   crm-app:latest
#
# Push to AWS ECR:
# aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
# docker tag crm-app:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
# docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/crm-app:latest
