# ============================================================
# Stage 1: Builder
# ============================================================
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy Maven build descriptor first for dependency caching
COPY pom.xml .

# Download all dependencies (layer cache optimization)
RUN mvn dependency:go-offline -B

# Copy the full source code
COPY src ./src

# Build the application JAR (skip tests for Docker build)
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM amazoncorretto:8

# Set working directory
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup -s /bin/false appuser

# Set timezone
ENV TZ=UTC

# JVM tuning for containerized environments
ENV JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=UTC"

# Spring Boot environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8080

# Database environment variables (override at runtime)
ENV DB_URL=jdbc:h2:mem:crmdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
ENV DB_USERNAME=sa
ENV DB_PASSWORD=

# Copy the built JAR from the builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Run the application with JVM options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
