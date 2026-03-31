FROM openjdk:8-jre-alpine

# Add application user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy the application JAR
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to application user
RUN chown -R spring:spring /app

# Switch to application user
USER spring:spring

# Expose application port (configurable via environment variable)
EXPOSE 8080

# Health check for cloud orchestration (Kubernetes, ECS)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/appinfo/health || exit 1

# JVM options for cloud deployment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
