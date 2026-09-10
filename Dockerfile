# ============================================================
# Stage 1: Maven Build
# Builds the Spring Boot CRM application JAR
# ============================================================
FROM maven:3.8.6-eclipse-temurin-8 AS builder

WORKDIR /workspace

# Copy pom.xml first for dependency layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application JAR (skip tests for container build)
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime Image
# Uses explicit base image: eclipse-temurin:8-jdk
# ============================================================
FROM eclipse-temurin:8-jdk

WORKDIR /app

# Create non-root user for security best practices
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the built JAR from the builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Set ownership
RUN chown appuser:appgroup app.jar

USER appuser

# Expose application port
EXPOSE 8080

# JVM options for container-aware memory management
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
