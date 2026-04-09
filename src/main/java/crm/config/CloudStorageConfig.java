package crm.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cloud Storage Configuration for Google Cloud Platform.
 * Provides centralized GCS client configuration for the application.
 */
@Configuration
@Slf4j
public class CloudStorageConfig {

    @Value("${gcs.project.id:${GCP_PROJECT_ID:}}")
    private String projectId;

    @Value("${gcs.bucket.name:${GCS_BUCKET_NAME:default-bucket}}")
    private String bucketName;

    /**
     * Creates a singleton Storage client bean for GCS operations.
     * Uses Application Default Credentials (ADC) for authentication in cloud environments.
     * 
     * @return Configured Storage client
     */
    @Bean
    public Storage googleCloudStorage() {
        try {
            StorageOptions.Builder builder = StorageOptions.newBuilder();
            
            // Only set project ID if explicitly provided
            if (projectId != null && !projectId.isEmpty()) {
                builder.setProjectId(projectId);
                log.info("Initializing GCS client with project ID: {}", projectId);
            } else {
                log.info("Initializing GCS client with default credentials (ADC)");
            }
            
            Storage storage = builder.build().getService();
            log.info("Google Cloud Storage client initialized successfully. Default bucket: {}", bucketName);
            return storage;
            
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Storage client", e);
            throw new RuntimeException("Could not initialize GCS client", e);
        }
    }
}
