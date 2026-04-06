package crm.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Cloud-ready utility for reading data files
 * Replaced Swing file chooser with classpath resource loading for cloud compatibility
 */
@Slf4j
public class ReadDataUtils {

    /**
     * Read file from classpath resources (cloud-compatible)
     * @param resourcePath Path to resource in classpath (e.g., "data/file.csv")
     * @return InputStream of the resource
     * @throws IOException if resource not found
     */
    public static InputStream readFileFromClasspath(String resourcePath) throws IOException {
        try {
            Resource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.error("Resource not found in classpath: {}", resourcePath);
                throw new IOException("Resource not found: " + resourcePath);
            }
            log.info("Successfully loaded resource from classpath: {}", resourcePath);
            return resource.getInputStream();
        } catch (IOException e) {
            log.error("Failed to read resource from classpath: {}", resourcePath, e);
            throw e;
        }
    }

    /**
     * Read file content from classpath as byte array
     * @param resourcePath Path to resource in classpath
     * @return byte array of file content
     * @throws IOException if resource not found or read fails
     */
    public static byte[] readFileContentFromClasspath(String resourcePath) throws IOException {
        try (InputStream inputStream = readFileFromClasspath(resourcePath)) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Check if resource exists in classpath
     * @param resourcePath Path to resource in classpath
     * @return true if resource exists
     */
    public static boolean resourceExists(String resourcePath) {
        try {
            Resource resource = new ClassPathResource(resourcePath);
            return resource.exists();
        } catch (Exception e) {
            log.warn("Error checking resource existence: {}", resourcePath, e);
            return false;
        }
    }
}
