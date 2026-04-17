package crm.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for Google Cloud Storage operations.
 * Provides cloud-native file storage capabilities.
 */
@Service
@Slf4j
public class CloudStorageService {

    private final Storage storage;
    private final String bucketName;
    private final ExecutorService executorService;

    @Autowired
    public CloudStorageService(Storage storage, @Value("${gcs.bucket.name}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * Uploads a file to Google Cloud Storage.
     * 
     * @param blobName The name/path of the blob in GCS
     * @param content The file content as byte array
     * @param contentType The MIME type of the file
     * @return The blob name if successful, null otherwise
     */
    public String uploadFile(String blobName, byte[] content, String contentType) {
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
            return null;
        }
    }

    /**
     * Asynchronously uploads a file to Google Cloud Storage.
     * 
     * @param blobName The name/path of the blob in GCS
     * @param content The file content as byte array
     * @param contentType The MIME type of the file
     * @return CompletableFuture with the blob name
     */
    public CompletableFuture<String> uploadFileAsync(String blobName, byte[] content, String contentType) {
        return CompletableFuture.supplyAsync(() -> uploadFile(blobName, content, contentType), executorService);
    }

    /**
     * Downloads a file from Google Cloud Storage.
     * 
     * @param blobName The name/path of the blob in GCS
     * @return InputStream of the file content, or null if not found
     */
    public InputStream downloadFile(String blobName) {
        try {
            Blob blob = storage.get(bucketName, blobName);
            if (blob == null) {
                log.warn("Blob not found in GCS: {}/{}", bucketName, blobName);
                return null;
            }
            
            byte[] content = blob.getContent();
            log.info("File downloaded successfully from GCS: {}/{}", bucketName, blobName);
            return new ByteArrayInputStream(content);
        } catch (Exception e) {
            log.error("Error downloading file from GCS: {}/{}", bucketName, blobName, e);
            return null;
        }
    }

    /**
     * Asynchronously downloads a file from Google Cloud Storage.
     * 
     * @param blobName The name/path of the blob in GCS
     * @return CompletableFuture with InputStream of the file content
     */
    public CompletableFuture<InputStream> downloadFileAsync(String blobName) {
        return CompletableFuture.supplyAsync(() -> downloadFile(blobName), executorService);
    }

    /**
     * Downloads a file from GCS to a temporary location.
     * 
     * @param blobName The name/path of the blob in GCS
     * @return Path to the temporary file, or null if failed
     */
    public Path downloadToTempFile(String blobName) {
        try {
            Blob blob = storage.get(bucketName, blobName);
            if (blob == null) {
                log.warn("Blob not found in GCS: {}/{}", bucketName, blobName);
                return null;
            }
            
            // Create temporary file
            String sanitizedName = blobName.replaceAll("[^a-zA-Z0-9.-]", "_");
            Path tempFile = Files.createTempFile("gcs-download-", "-" + sanitizedName);
            
            // Download blob content to temp file
            blob.downloadTo(tempFile);
            
            log.info("File downloaded to temp location: {}", tempFile);
            return tempFile;
        } catch (Exception e) {
            log.error("Error downloading file to temp location: {}/{}", bucketName, blobName, e);
            return null;
        }
    }

    /**
     * Deletes a file from Google Cloud Storage.
     * 
     * @param blobName The name/path of the blob in GCS
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteFile(String blobName) {
        try {
            boolean deleted = storage.delete(bucketName, blobName);
            if (deleted) {
                log.info("File deleted successfully from GCS: {}/{}", bucketName, blobName);
            } else {
                log.warn("File not found for deletion: {}/{}", bucketName, blobName);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Error deleting file from GCS: {}/{}", bucketName, blobName, e);
            return false;
        }
    }

    /**
     * Checks if a file exists in Google Cloud Storage.
     * 
     * @param blobName The name/path of the blob in GCS
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
     * Lists all files in a specific folder/prefix.
     * 
     * @param prefix The folder prefix
     * @return Iterable of blobs
     */
    public Iterable<Blob> listFiles(String prefix) {
        try {
            return storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();
        } catch (Exception e) {
            log.error("Error listing files from GCS with prefix: {}", prefix, e);
            return null;
        }
    }

    /**
     * Shuts down the executor service.
     * Should be called when the application is shutting down.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
