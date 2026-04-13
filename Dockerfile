# Multi-stage build for Spring Boot application
# Stage 1: Build stage
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /workspace

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:8-jre

WORKDIR /app

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built artifact from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Set timezone
ENV TZ=UTC

# Set Spring profile for Docker
ENV SPRING_PROFILES_ACTIVE=docker

# Expose application port
EXPOSE 8080

# Expose management/actuator port
EXPOSE 8081

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
