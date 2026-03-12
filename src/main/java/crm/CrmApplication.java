package crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

/**
 * Cloud-ready CRM Application
 * - Supports cloud-native buildpacks for containerization
 * - Distributed caching enabled
 * - Stateless architecture
 * - Compatible with AWS, Azure, GCP
 * - Horizontal scaling support
 * - Health checks and metrics enabled
 */
@EntityScan(
        basePackageClasses = {CrmApplication.class, Jsr310JpaConverters.class}
)
@SpringBootApplication
@EnableCaching  // Enable distributed caching for cloud scalability
public class CrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmApplication.class, args);
    }

}
