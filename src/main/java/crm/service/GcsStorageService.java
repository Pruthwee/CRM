package crm.service;

import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for Google Cloud Storage operations.
 * Provides centralized, cloud-native file storage operations.
 */
@Service
@Slf4j
public class GcsStorageService {

    private final Storage storage;
    private final String bucketName;
    private final ExecutorService executorService;

    @Autowired
    public GcsStorageService(Storage storage, String gcsBucketName) {
        this.storage = storage;
        this.bucketName = gcsBucketName;
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
        log.info("GcsStorageService initialized with bucket: {}", bucketName);
    }

    /**
     * Upload file to GCS asynchronously
     * @param blobName Name/path of the blob in GCS
     * @param content File content as byte array
     * @param contentType MIME type of the content
     * @return CompletableFuture with blob name
     */
    public CompletableFuture<String> uploadFileAsync(String blobName, byte[] content, String contentType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BlobId blobId = BlobId.of(bucketName, blobName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType(contentType)
                        .build();
                
                storage.create(blobInfo, content);
                log.info("File uploaded successfully to GCS: {}/{}", bucketName, blobName);
                
                return blobName;
            } catch (Exception e) {
                log.error("Error uploading file to GCS: {}/{}", bucketName, blobName, e);
                throw new RuntimeException("Failed to upload file to GCS", e);
            }
        }, executorService);
    }

    /**
     * Download file from GCS asynchronously
     * @param blobName Name/path of the blob in GCS
     * @return CompletableFuture with file content as byte array
     */
    public CompletableFuture<byte[]> downloadFileAsync(String blobName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Blob blob = storage.get(bucketName, blobName);
                if (blob == null) {
                    log.warn("Blob not found in GCS: {}/{}", bucketName, blobName);
                    return null;
                }
                
                byte[] content = blob.getContent();
                log.info("File downloaded successfully from GCS: {}/{}", bucketName, blobName);
                
                return content;
            } catch (Exception e) {
                log.error("Error downloading file from GCS: {}/{}", bucketName, blobName, e);
                throw new RuntimeException("Failed to download file from GCS", e);
            }
        }, executorService);
    }

    /**
     * Get file as InputStream from GCS
     * @param blobName Name/path of the blob in GCS
     * @return InputStream of the file content
     */
    public InputStream getFileAsStream(String blobName) {
        try {
            Blob blob = storage.get(bucketName, blobName);
            if (blob == null) {
                log.warn("Blob not found in GCS: {}/{}", bucketName, blobName);
                return null;
            }
            
            byte[] content = blob.getContent();
            return new ByteArrayInputStream(content);
        } catch (Exception e) {
            log.error("Error getting file stream from GCS: {}/{}", bucketName, blobName, e);
            return null;
        }
    }

    /**
     * Delete file from GCS
     * @param blobName Name/path of the blob in GCS
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteFile(String blobName) {
        try {
            boolean deleted = storage.delete(bucketName, blobName);
            if (deleted) {
                log.info("File deleted successfully from GCS: {}/{}", bucketName, blobName);
            } else {
                log.warn("File not found for deletion in GCS: {}/{}", bucketName, blobName);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Error deleting file from GCS: {}/{}", bucketName, blobName, e);
            return false;
        }
    }

    /**
     * Check if file exists in GCS
     * @param blobName Name/path of the blob in GCS
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String blobName) {
        try {
            Blob blob = storage.get(bucketName, blobName);
            return blob != null && blob.exists();
        } catch (Exception e) {
            log.error("Error checking file existence in GCS: {}/{}", bucketName, blobName, e);
            return false;
        }
    }

    /**
     * List files in GCS with prefix
     * @param prefix Prefix to filter blobs
     * @return List of blob names
     */
    public List<String> listFiles(String prefix) {
        List<String> fileNames = new ArrayList<>();
        try {
            Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix));
            
            for (Blob blob : blobs.iterateAll()) {
                fileNames.add(blob.getName());
            }
            
            log.info("Listed {} files in GCS with prefix: {}", fileNames.size(), prefix);
        } catch (Exception e) {
            log.error("Error listing files in GCS with prefix: {}", prefix, e);
        }
        
        return fileNames;
    }

    /**
     * Get signed URL for temporary access to file
     * @param blobName Name/path of the blob in GCS
     * @param durationMinutes Duration in minutes for URL validity
     * @return Signed URL string
     */
    public String getSignedUrl(String blobName, int durationMinutes) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName).build();
            
            // Note: This requires service account credentials
            // For production, configure proper service account
            java.net.URL signedUrl = storage.signUrl(
                    blobInfo,
                    durationMinutes,
                    java.util.concurrent.TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature()
            );
            
            log.info("Generated signed URL for: {}/{}", bucketName, blobName);
            return signedUrl.toString();
        } catch (Exception e) {
            log.error("Error generating signed URL for: {}/{}", bucketName, blobName, e);
            return null;
        }
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        executorService.shutdown();
        log.info("GcsStorageService executor service shut down");
    }
}
