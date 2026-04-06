package crm.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Cloud-ready utility for reading data files.
 * Replaced Swing-based file chooser with classpath resource loading
 * to support cloud and containerized environments.
 */
@Slf4j
public class ReadDataUtils {

    /**
     * Read file from classpath resources instead of file system.
     * This approach works in cloud environments where file system access is restricted.
     * 
     * @param resourcePath the path to the resource in classpath (e.g., "data/file.csv")
     * @return InputStream of the resource
     * @throws IOException if resource cannot be found or read
     */
    public static InputStream readFileFromClasspath(String resourcePath) throws IOException {
        log.info("Reading file from classpath: {}", resourcePath);
        Resource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            log.error("Resource not found in classpath: {}", resourcePath);
            throw new IOException("Resource not found: " + resourcePath);
        }
        return resource.getInputStream();
    }

    /**
     * Check if a resource exists in the classpath
     * 
     * @param resourcePath the path to the resource in classpath
     * @return true if resource exists, false otherwise
     */
    public static boolean resourceExists(String resourcePath) {
        try {
            Resource resource = new ClassPathResource(resourcePath);
            return resource.exists();
        } catch (Exception e) {
            log.warn("Error checking resource existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get resource from classpath
     * 
     * @param resourcePath the path to the resource in classpath
     * @return Resource object
     */
    public static Resource getResource(String resourcePath) {
        log.debug("Getting resource from classpath: {}", resourcePath);
        return new ClassPathResource(resourcePath);
    }
}
