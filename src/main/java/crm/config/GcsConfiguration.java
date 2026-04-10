package crm.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google Cloud Storage configuration for cloud-native file operations.
 * Provides centralized GCS client configuration.
 */
@Configuration
@Slf4j
public class GcsConfiguration {

    @Value("${gcs.project.id:}")
    private String projectId;

    @Value("${gcs.bucket.name:crm-data-bucket}")
    private String bucketName;

    /**
     * Create GCS Storage client bean
     * @return Storage client instance
     */
    @Bean
    public Storage gcsStorage() {
        try {
            StorageOptions.Builder builder = StorageOptions.newBuilder();
            
            // Set project ID if provided
            if (projectId != null && !projectId.isEmpty()) {
                builder.setProjectId(projectId);
                log.info("GCS Storage initialized with project ID: {}", projectId);
            } else {
                log.info("GCS Storage initialized with default credentials");
            }
            
            Storage storage = builder.build().getService();
            log.info("GCS Storage client created successfully for bucket: {}", bucketName);
            
            return storage;
        } catch (Exception e) {
            log.error("Failed to initialize GCS Storage client", e);
            throw new RuntimeException("Failed to initialize GCS Storage", e);
        }
    }

    /**
     * Get configured bucket name
     * @return GCS bucket name
     */
    @Bean
    public String gcsBucketName() {
        return bucketName;
    }
}
