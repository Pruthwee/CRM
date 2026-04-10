package crm.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Azure Blob Storage configuration for cloud-native file operations.
 * Provides centralized configuration for Azure Storage services.
 */
@Configuration
public class AzureStorageConfig {

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    /**
     * Creates a BlobServiceClient bean for Azure Blob Storage operations.
     * Returns null if connection string is not configured (for local development).
     * 
     * @return BlobServiceClient instance or null if not configured
     */
    @Bean
    public BlobServiceClient blobServiceClient() {
        if (connectionString == null || connectionString.isEmpty()) {
            // Return null for local development without Azure Storage
            return null;
        }

        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}
