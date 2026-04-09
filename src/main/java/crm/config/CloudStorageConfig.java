package crm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Cloud-ready configuration for Google Cloud Storage.
 * Provides centralized GCS client configuration for the application.
 */
@Configuration
public class CloudStorageConfig {

    @Value("${spring.cloud.gcp.project-id:}")
    private String projectId;

    @Value("${spring.cloud.gcp.credentials.location:}")
    private String credentialsLocation;

    /**
     * Creates a Storage bean for Google Cloud Storage operations.
     * Uses Application Default Credentials when credentials location is not specified.
     * 
     * @return Storage client instance
     */
    @Bean
    public Storage storage() throws IOException {
        StorageOptions.Builder builder = StorageOptions.newBuilder();
        
        if (projectId != null && !projectId.isEmpty()) {
            builder.setProjectId(projectId);
        }
        
        if (credentialsLocation != null && !credentialsLocation.isEmpty()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsLocation)
            );
            builder.setCredentials(credentials);
        }
        // If no credentials specified, uses Application Default Credentials (ADC)
        // which works automatically in GCP environments (GKE, Cloud Run, etc.)
        
        return builder.build().getService();
    }
}
