package crm.utils;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Cloud-ready utility for reading data from Google Cloud Storage.
 * Replaces local file system dependencies with GCS operations.
 */
@Slf4j
public class ReadDataUtils {

    private static final String GCS_BUCKET_NAME = System.getenv().getOrDefault("GCS_BUCKET_NAME", "crm-data-bucket");
    private static Storage storage;

    static {
        try {
            storage = StorageOptions.getDefaultInstance().getService();
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Storage client", e);
        }
    }

    /**
     * Reads a file from Google Cloud Storage.
     * 
     * @param blobName The name/path of the blob in GCS
     * @return InputStream of the file content
     */
    public static InputStream readFileFromGCS(String blobName) {
        try {
            if (storage == null) {
                log.error("Google Cloud Storage client not initialized");
                return null;
            }
            
            Blob blob = storage.get(GCS_BUCKET_NAME, blobName);
            if (blob == null) {
                log.warn("Blob not found in GCS: {}/{}", GCS_BUCKET_NAME, blobName);
                return null;
            }
            
            byte[] content = blob.getContent();
            log.info("Successfully read file from GCS: {}/{}", GCS_BUCKET_NAME, blobName);
            return new ByteArrayInputStream(content);
        } catch (Exception e) {
            log.error("Error reading file from GCS: {}/{}", GCS_BUCKET_NAME, blobName, e);
            return null;
        }
    }

    /**
     * Downloads a file from GCS to a temporary location for processing.
     * 
     * @param blobName The name/path of the blob in GCS
     * @return Path to the temporary file
     */
    public static Path downloadFileFromGCS(String blobName) {
        try {
            if (storage == null) {
                log.error("Google Cloud Storage client not initialized");
                return null;
            }
            
            Blob blob = storage.get(GCS_BUCKET_NAME, blobName);
            if (blob == null) {
                log.warn("Blob not found in GCS: {}/{}", GCS_BUCKET_NAME, blobName);
                return null;
            }
            
            // Create temporary file
            Path tempFile = Files.createTempFile("gcs-download-", "-" + blobName.replaceAll("[^a-zA-Z0-9.-]", "_"));
            
            // Download blob content to temp file
            blob.downloadTo(tempFile);
            
            log.info("Successfully downloaded file from GCS to temp location: {}", tempFile);
            return tempFile;
        } catch (Exception e) {
            log.error("Error downloading file from GCS: {}/{}", GCS_BUCKET_NAME, blobName, e);
            return null;
        }
    }

    /**
     * Lists all blobs in the configured GCS bucket with a given prefix.
     * 
     * @param prefix The prefix to filter blobs
     * @return Iterable of blob names
     */
    public static Iterable<Blob> listFilesFromGCS(String prefix) {
        try {
            if (storage == null) {
                log.error("Google Cloud Storage client not initialized");
                return null;
            }
            
            return storage.list(GCS_BUCKET_NAME, Storage.BlobListOption.prefix(prefix)).iterateAll();
        } catch (Exception e) {
            log.error("Error listing files from GCS with prefix: {}", prefix, e);
            return null;
        }
    }
}
