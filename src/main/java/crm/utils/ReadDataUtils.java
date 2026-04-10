package crm.utils;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            log.error("Failed to initialize GCS Storage client", e);
        }
    }

    /**
     * Read file from Google Cloud Storage
     * @param blobName The name/path of the blob in GCS
     * @return InputStream of the file content
     */
    public static InputStream readFileFromGCS(String blobName) {
        try {
            if (storage == null) {
                log.error("GCS Storage client not initialized");
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
     * Read file from classpath resources (for configuration files)
     * @param resourcePath Path to resource in classpath
     * @return InputStream of the resource
     */
    public static InputStream readFileFromClasspath(String resourcePath) {
        try {
            InputStream inputStream = ReadDataUtils.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                log.warn("Resource not found in classpath: {}", resourcePath);
                return null;
            }
            log.info("Successfully read file from classpath: {}", resourcePath);
            return inputStream;
        } catch (Exception e) {
            log.error("Error reading file from classpath: {}", resourcePath, e);
            return null;
        }
    }

    /**
     * List files in GCS bucket with prefix
     * @param prefix The prefix to filter blobs
     * @return Iterable of blob names
     */
    public static Iterable<Blob> listFilesInGCS(String prefix) {
        try {
            if (storage == null) {
                log.error("GCS Storage client not initialized");
                return null;
            }
            
            return storage.list(GCS_BUCKET_NAME, Storage.BlobListOption.prefix(prefix)).iterateAll();
        } catch (Exception e) {
            log.error("Error listing files in GCS with prefix: {}", prefix, e);
            return null;
        }
    }

    /**
     * Check if blob exists in GCS
     * @param blobName The name/path of the blob
     * @return true if exists, false otherwise
     */
    public static boolean fileExistsInGCS(String blobName) {
        try {
            if (storage == null) {
                return false;
            }
            Blob blob = storage.get(GCS_BUCKET_NAME, blobName);
            return blob != null && blob.exists();
        } catch (Exception e) {
            log.error("Error checking file existence in GCS: {}", blobName, e);
            return false;
        }
    }
}
