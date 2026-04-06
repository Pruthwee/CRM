package crm.service;

import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Cloud Storage Service for GCP
 * Provides cloud-native file storage operations replacing local file system dependencies
 */
@Service
@Slf4j
public class CloudStorageService {

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${gcp.storage.project-id:#{null}}")
    private String projectId;

    @Value("${gcp.storage.credentials-path:#{null}}")
    private String credentialsPath;

    private Storage storage;

    @PostConstruct
    public void init() {
        try {
            StorageOptions.Builder builder = StorageOptions.newBuilder();
            
            if (projectId != null && !projectId.isEmpty()) {
                builder.setProjectId(projectId);
            }
            
            if (credentialsPath != null && !credentialsPath.isEmpty() && Files.exists(Paths.get(credentialsPath))) {
                builder.setCredentials(
                    com.google.auth.oauth2.GoogleCredentials.fromStream(
                        Files.newInputStream(Paths.get(credentialsPath))
                    )
                );
            }
            
            storage = builder.build().getService();
            log.info("Cloud Storage Service initialized successfully for bucket: {}", bucketName);
        } catch (Exception e) {
            log.warn("Cloud Storage Service initialization failed, using fallback mode: {}", e.getMessage());
            // Fallback to default credentials or local mode
            storage = StorageOptions.getDefaultInstance().getService();
        }
    }

    /**
     * Upload file content to GCP Cloud Storage
     * @param fileName Name of the file
     * @param content File content as byte array
     * @param contentType MIME type of the content
     * @return URL or path to the uploaded file
     */
    public String uploadFile(String fileName, byte[] content, String contentType) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();
            
            Blob blob = storage.create(blobInfo, content);
            log.info("File uploaded successfully to Cloud Storage: {}", fileName);
            return String.format("gs://%s/%s", bucketName, fileName);
        } catch (Exception e) {
            log.error("Failed to upload file to Cloud Storage: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to cloud storage", e);
        }
    }

    /**
     * Upload file content from InputStream to GCP Cloud Storage
     * @param fileName Name of the file
     * @param inputStream Input stream of file content
     * @param contentType MIME type of the content
     * @return URL or path to the uploaded file
     */
    public String uploadFile(String fileName, InputStream inputStream, String contentType) throws IOException {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();
            
            byte[] content = inputStream.readAllBytes();
            Blob blob = storage.create(blobInfo, content);
            log.info("File uploaded successfully to Cloud Storage: {}", fileName);
            return String.format("gs://%s/%s", bucketName, fileName);
        } catch (Exception e) {
            log.error("Failed to upload file to Cloud Storage: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to cloud storage", e);
        }
    }

    /**
     * Download file from GCP Cloud Storage
     * @param fileName Name of the file
     * @return File content as byte array
     */
    public byte[] downloadFile(String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            Blob blob = storage.get(blobId);
            
            if (blob == null) {
                log.error("File not found in Cloud Storage: {}", fileName);
                throw new RuntimeException("File not found: " + fileName);
            }
            
            log.info("File downloaded successfully from Cloud Storage: {}", fileName);
            return blob.getContent();
        } catch (Exception e) {
            log.error("Failed to download file from Cloud Storage: {}", fileName, e);
            throw new RuntimeException("Failed to download file from cloud storage", e);
        }
    }

    /**
     * Delete file from GCP Cloud Storage
     * @param fileName Name of the file
     * @return true if deleted successfully
     */
    public boolean deleteFile(String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                log.info("File deleted successfully from Cloud Storage: {}", fileName);
            } else {
                log.warn("File not found for deletion in Cloud Storage: {}", fileName);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete file from Cloud Storage: {}", fileName, e);
            return false;
        }
    }

    /**
     * Check if file exists in GCP Cloud Storage
     * @param fileName Name of the file
     * @return true if file exists
     */
    public boolean fileExists(String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();
        } catch (Exception e) {
            log.error("Failed to check file existence in Cloud Storage: {}", fileName, e);
            return false;
        }
    }
}
