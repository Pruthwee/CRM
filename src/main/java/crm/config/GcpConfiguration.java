package crm.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google Cloud Platform configuration for cloud-native services.
 * Provides beans for GCS (Cloud Storage) and Firestore.
 */
@Configuration
@Slf4j
public class GcpConfiguration {

    @Value("${gcp.storage.bucket:}")
    private String gcsBucketName;

    @Value("${gcp.firestore.project-id:}")
    private String firestoreProjectId;

    /**
     * Creates a Google Cloud Storage client bean.
     * Uses Application Default Credentials (ADC) for authentication.
     * 
     * @return Storage client instance
     */
    @Bean
    public Storage googleCloudStorage() {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        log.info("Google Cloud Storage client initialized: bucket={}", gcsBucketName);
        return storage;
    }

    /**
     * Creates a Firestore client bean.
     * Uses Application Default Credentials (ADC) for authentication.
     * 
     * @return Firestore client instance
     */
    @Bean
    public Firestore firestore() {
        FirestoreOptions.Builder optionsBuilder = FirestoreOptions.newBuilder();
        
        // Set project ID if provided
        if (firestoreProjectId != null && !firestoreProjectId.isEmpty()) {
            optionsBuilder.setProjectId(firestoreProjectId);
        }
        
        Firestore firestore = optionsBuilder.build().getService();
        log.info("Firestore client initialized: projectId={}", 
                 firestoreProjectId != null && !firestoreProjectId.isEmpty() 
                     ? firestoreProjectId 
                     : "default");
        return firestore;
    }

}
