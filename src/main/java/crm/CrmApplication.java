package crm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * CRM Application - Cloud-Ready Version
 * 
 * Cloud readiness improvements:
 * - JAR packaging with embedded Tomcat
 * - Externalized configuration via environment variables
 * - UTC timezone for consistency across distributed systems
 * - Connection pooling for database resilience
 * - Azure Blob Storage integration for file operations
 */
@EntityScan(
        basePackageClasses = {CrmApplication.class, Jsr310JpaConverters.class}
)
@SpringBootApplication
@Slf4j
public class CrmApplication {

    /**
     * Initialize application with cloud-ready defaults
     */
    @PostConstruct
    public void init() {
        // Set default timezone to UTC for cloud consistency
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Application timezone set to UTC for cloud consistency");
        log.info("Default ZoneId: {}", ZoneId.systemDefault());
        
        // Log startup information
        log.info("CRM Application starting in cloud-ready mode");
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Spring Boot Version: {}", SpringApplication.class.getPackage().getImplementationVersion());
    }

    public static void main(String[] args) {
        // Set system properties for cloud deployment
        System.setProperty("user.timezone", "UTC");
        
        log.info("Starting CRM Application...");
        SpringApplication.run(CrmApplication.class, args);
    }
}
