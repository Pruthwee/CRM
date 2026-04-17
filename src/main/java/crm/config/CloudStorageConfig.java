package crm.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Google Cloud Storage integration.
 * Provides cloud-native storage capabilities for the application.
 */
@Configuration
@Slf4j
public class CloudStorageConfig {

    @Value("${gcs.bucket.name:crm-data-bucket}")
    private String bucketName;

    @Value("${gcs.project.id:}")
    private String projectId;

    /**
     * Creates a Google Cloud Storage client bean.
     * Uses Application Default Credentials (ADC) for authentication.
     * 
     * In GCP environments (Cloud Run, GKE, Compute Engine), ADC automatically
     * uses the service account attached to the resource.
     * 
     * For local development, set GOOGLE_APPLICATION_CREDENTIALS environment
     * variable to point to a service account key file.
     */
    @Bean
    public Storage googleCloudStorage() {
        try {
            StorageOptions.Builder builder = StorageOptions.newBuilder();
            
            // Set project ID if provided
            if (projectId != null && !projectId.isEmpty()) {
                builder.setProjectId(projectId);
                log.info("Initializing Google Cloud Storage with project ID: {}", projectId);
            } else {
                log.info("Initializing Google Cloud Storage with default project from ADC");
            }
            
            Storage storage = builder.build().getService();
            log.info("Google Cloud Storage client initialized successfully. Bucket: {}", bucketName);
            
            return storage;
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Storage client. " +
                    "Ensure GOOGLE_APPLICATION_CREDENTIALS is set or running in GCP environment.", e);
            throw new RuntimeException("Cloud Storage initialization failed", e);
        }
    }

    /**
     * Returns the configured GCS bucket name.
     */
    @Bean
    public String gcsBucketName() {
        return bucketName;
    }
}
