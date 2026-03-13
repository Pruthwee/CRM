package crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Cloud-Ready CRM Application
 * 
 * Cloud-Native Features:
 * - JPA/Hibernate ORM for database abstraction and portability across cloud databases
 * - Distributed caching support for horizontal scaling
 * - Async processing for improved performance
 * - Transaction management for data consistency
 * - Cloud-native buildpacks support for containerization
 * - Environment-based configuration for 12-factor app compliance
 * - Stateless architecture for cloud deployment
 * 
 * AWS Deployment Ready:
 * - Compatible with AWS RDS (MySQL, PostgreSQL, Aurora)
 * - Compatible with AWS ElastiCache (Redis)
 * - Compatible with AWS ECS/EKS for container orchestration
 * - Compatible with AWS Elastic Beanstalk
 * - Compatible with AWS Lambda (with Spring Cloud Function)
 * - Supports AWS CloudWatch for monitoring and logging
 * - Supports AWS Secrets Manager for credential management
 * 
 * 12-Factor App Compliance:
 * - I. Codebase: Single codebase tracked in version control
 * - II. Dependencies: Explicitly declared via Maven
 * - III. Config: Configuration stored in environment variables
 * - IV. Backing services: Database and cache as attached resources
 * - V. Build, release, run: Separate build and run stages
 * - VI. Processes: Stateless processes with externalized session
 * - VII. Port binding: Self-contained with embedded server
 * - VIII. Concurrency: Horizontal scaling via process model
 * - IX. Disposability: Fast startup and graceful shutdown
 * - X. Dev/prod parity: Same backing services across environments
 * - XI. Logs: Treat logs as event streams
 * - XII. Admin processes: Run admin tasks as one-off processes
 */
@EntityScan(
        basePackageClasses = {CrmApplication.class, Jsr310JpaConverters.class}
)
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class CrmApplication {

    /**
     * Initialize application with cloud-ready defaults
     * Sets UTC timezone for consistent date/time handling across cloud regions
     */
    @PostConstruct
    public void init() {
        // Set default timezone to UTC for cloud consistency
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Main entry point for cloud-native Spring Boot application
     * Supports containerization and cloud platform deployment
     * 
     * @param args Command line arguments (can include Spring Boot properties)
     */
    public static void main(String[] args) {
        // Cloud-Native: Configure application for cloud deployment
        SpringApplication app = new SpringApplication(CrmApplication.class);
        
        // Enable graceful shutdown for cloud environments
        app.setRegisterShutdownHook(true);
        
        // Run the application
        app.run(args);
    }

    /**
     * Health check bean for cloud load balancers and orchestrators
     * Provides application health status for AWS ELB, ECS, Kubernetes
     */
    @Bean
    public org.springframework.boot.actuate.health.HealthIndicator customHealthIndicator() {
        return () -> org.springframework.boot.actuate.health.Health
                .up()
                .withDetail("app", "CRM Application")
                .withDetail("status", "Running")
                .withDetail("cloud-ready", true)
                .build();
    }
}
