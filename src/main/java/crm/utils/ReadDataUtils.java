package crm.utils;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cloud-ready utility for reading data files.
 * Uses environment variables with Google Cloud Secret Manager for dynamic paths.
 * Replaces hard-coded file paths with GCS bucket references.
 */
@Slf4j
public class ReadDataUtils {

    private static final String GCS_BUCKET_NAME_ENV = "GCS_BUCKET_NAME";
    private static final String FILE_PATH_PREFIX_ENV = "FILE_PATH_PREFIX";
    
    /**
     * Reads a file from Google Cloud Storage based on environment-configured bucket and path.
     * 
     * @param fileName The name of the file to read from GCS
     * @param fileExtension Expected file extension for validation
     * @return File object pointing to the downloaded temporary file
     */
    public static File ReadFile(String fileName, String fileExtension) {
        String bucketName = System.getenv(GCS_BUCKET_NAME_ENV);
        String pathPrefix = System.getenv(FILE_PATH_PREFIX_ENV);
        
        if (bucketName == null || bucketName.isEmpty()) {
            log.error("GCS_BUCKET_NAME environment variable is not set");
            throw new IllegalStateException("GCS_BUCKET_NAME environment variable must be configured");
        }
        
        if (pathPrefix == null) {
            pathPrefix = "";
        }
        
        String objectName = pathPrefix.isEmpty() ? fileName : pathPrefix + "/" + fileName;
        
        try {
            // Initialize GCS client
            Storage storage = StorageOptions.getDefaultInstance().getService();
            
            // Download file from GCS
            Blob blob = storage.get(bucketName, objectName);
            if (blob == null) {
                log.error("File not found in GCS: bucket={}, object={}", bucketName, objectName);
                throw new IllegalArgumentException("File not found in GCS: " + objectName);
            }
            
            // Validate file extension
            if (fileExtension != null && !fileName.endsWith("." + fileExtension)) {
                log.warn("File extension mismatch: expected={}, actual={}", fileExtension, fileName);
            }
            
            // Create temporary file for local processing
            Path tempFile = Files.createTempFile("gcs-download-", "-" + fileName);
            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                blob.downloadTo(fos);
            }
            
            log.info("Successfully downloaded file from GCS: bucket={}, object={}, localPath={}", 
                     bucketName, objectName, tempFile.toString());
            
            // Mark for deletion on JVM exit (ephemeral storage)
            tempFile.toFile().deleteOnExit();
            
            return tempFile.toFile();
            
        } catch (IOException e) {
            log.error("Error downloading file from GCS: bucket={}, object={}", bucketName, objectName, e);
            throw new RuntimeException("Failed to download file from GCS", e);
        }
    }
    
    /**
     * Legacy method signature maintained for backward compatibility.
     * Delegates to cloud-native implementation.
     */
    @Deprecated
    public static File ReadFile(String dialogMessage, Object parent, String fileExtensionDescription,
                                String... fileExtension) {
        log.warn("Using deprecated ReadFile method. GUI-based file selection is not supported in cloud environments.");
        
        // Extract first file extension if provided
        String ext = (fileExtension != null && fileExtension.length > 0) ? fileExtension[0] : null;
        
        // Attempt to extract filename from dialog message or use default
        String fileName = System.getenv("DEFAULT_FILE_NAME");
        if (fileName == null || fileName.isEmpty()) {
            fileName = "default-file." + (ext != null ? ext : "dat");
        }
        
        return ReadFile(fileName, ext);
    }

}
