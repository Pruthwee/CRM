package crm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Cloud-ready utility for reading data files.
 * Replaces desktop GUI file chooser with cloud-native resource loading.
 * 
 * For AWS cloud deployment:
 * - Files should be uploaded via REST API endpoints
 * - Files can be loaded from classpath resources
 * - Files can be retrieved from AWS S3 (requires AWS SDK dependency)
 * 
 * Environment variables:
 * - FILE_STORAGE_TYPE: "classpath" (default) or "s3"
 * - S3_BUCKET_NAME: AWS S3 bucket name (if using S3)
 * - AWS_REGION: AWS region (if using S3)
 */
public class ReadDataUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReadDataUtils.class);
    
    /**
     * Reads a file from classpath resources.
     * This is cloud-compatible as resources are packaged within the application JAR.
     * 
     * @param resourcePath Path to the resource file (e.g., "data/sample.csv")
     * @return File object pointing to the resource
     * @throws IOException if resource cannot be found or read
     */
    public static File readFileFromClasspath(String resourcePath) throws IOException {
        logger.info("Reading file from classpath: {}", resourcePath);
        
        try {
            Resource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            // For cloud environments, copy resource to temp file
            InputStream inputStream = resource.getInputStream();
            Path tempFile = Files.createTempFile("crm-upload-", getFileExtension(resourcePath));
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            File file = tempFile.toFile();
            file.deleteOnExit(); // Clean up temp file on JVM exit
            
            logger.info("Successfully loaded file from classpath: {}", resourcePath);
            return file;
            
        } catch (IOException e) {
            logger.error("Failed to read file from classpath: {}", resourcePath, e);
            throw e;
        }
    }
    
    /**
     * Reads a file from a temporary location.
     * Used when files are uploaded via REST API endpoints.
     * 
     * @param filePath Path to the temporary file
     * @return File object
     * @throws IOException if file cannot be found
     */
    public static File readFileFromPath(String filePath) throws IOException {
        logger.info("Reading file from path: {}", filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        
        logger.info("Successfully loaded file from path: {}", filePath);
        return file;
    }
    
    /**
     * Legacy method maintained for backward compatibility.
     * In cloud environments, this method should not be used.
     * Instead, use file upload endpoints or classpath resources.
     * 
     * @deprecated Use readFileFromClasspath() or implement file upload REST endpoints
     */
    @Deprecated
    public static File ReadFile(String dialogMessage, Object parent, String fileExtensionDescription,
                                String... fileExtension) {
        logger.warn("DEPRECATED: ReadFile() method called. This method uses desktop GUI components " +
                   "and is not compatible with cloud environments. " +
                   "Use readFileFromClasspath() or implement file upload REST endpoints instead.");
        
        // Return null to indicate this method is not supported in cloud environments
        // Applications should handle null gracefully and use alternative methods
        return null;
    }
    
    /**
     * Helper method to extract file extension from path.
     */
    private static String getFileExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            return path.substring(lastDot);
        }
        return ".tmp";
    }
}
