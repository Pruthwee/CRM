package crm.utils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cloud-ready utility for reading data from Google Cloud Storage.
 * Replaces desktop-based file chooser with GCS operations.
 */
@Component
@Slf4j
public class ReadDataUtils {

    private final Storage storage;

    public ReadDataUtils(Storage storage) {
        this.storage = storage;
    }

    /**
            Blob blob = storage.get(bucketName, blobName);
            if (blob == null) {
                log.error("Blob not found in GCS: bucket={}, blob={}", bucketName, blobName);
                return null;
            }
            
            byte[] content = blob.getContent();
            log.info("Successfully read file from GCS: bucket={}, blob={}, size={} bytes", 
                    bucketName, blobName, content.length);
            return new ByteArrayInputStream(content);
        } catch (Exception e) {
            log.error("Error reading file from GCS: bucket={}, blob={}", bucketName, blobName, e);
            return null;
        }
    }

    /**
     * Reads a file from classpath resources (for bundled configuration files).
     * 
     * @param resourcePath The classpath resource path
     * @return InputStream of the resource
     */
    public InputStream readFileFromClasspath(String resourcePath) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                log.error("Resource not found in classpath: {}", resourcePath);
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
     * For local development/testing only - reads from local filesystem.
     * Should not be used in production cloud environments.
     * 
     * @param filePath The local file path
     * @return InputStream of the file
     * @deprecated Use readFileFromGCS for cloud deployments
     */
    @Deprecated
    public InputStream readFileFromLocalPath(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("Local file not found: {}", filePath);
                return null;
            }
            log.warn("Reading from local filesystem - not recommended for cloud deployments: {}", filePath);
            return Files.newInputStream(path);
        } catch (Exception e) {
            log.error("Error reading local file: {}", filePath, e);
            return null;
        }
    }
}
