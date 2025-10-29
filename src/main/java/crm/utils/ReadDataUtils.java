package crm.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class ReadDataUtils {

    private final ResourceLoader resourceLoader;

    public ReadDataUtils(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Cloud-compatible file reading utility that works with classpath resources
     * and external file paths using environment variables
     */
    public InputStream readFileAsStream(String fileName) throws IOException {
        // First try to read from classpath (cloud-friendly)
        try {
            Resource resource = resourceLoader.getResource("classpath:data/" + fileName);
            if (resource.exists()) {
                log.info("Reading file from classpath: {}", fileName);
                return resource.getInputStream();
            }
        } catch (IOException e) {
            log.debug("File not found in classpath: {}, trying external path", fileName);
        }

        // Fallback to external file path from environment variable
        String dataPath = System.getenv("DATA_FILES_PATH");
        if (dataPath != null) {
            Path filePath = Paths.get(dataPath, fileName);
            if (Files.exists(filePath)) {
                log.info("Reading file from external path: {}", filePath);
                return Files.newInputStream(filePath);
            }
        }

        // Last resort: try default temp directory
        Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), "crm-data", fileName);
        if (Files.exists(tempFilePath)) {
            log.info("Reading file from temp directory: {}", tempFilePath);
            return Files.newInputStream(tempFilePath);
        }

        throw new IOException("File not found: " + fileName + ". Checked classpath, DATA_FILES_PATH, and temp directory.");
    }

    /**
     * Check if a file exists in any of the supported locations
     */
    public boolean fileExists(String fileName) {
        // Check classpath
        try {
            Resource resource = resourceLoader.getResource("classpath:data/" + fileName);
            if (resource.exists()) {
                return true;
            }
        } catch (Exception e) {
            log.debug("Error checking classpath for file: {}", fileName);
        }

        // Check external path
        String dataPath = System.getenv("DATA_FILES_PATH");
        if (dataPath != null) {
            Path filePath = Paths.get(dataPath, fileName);
            if (Files.exists(filePath)) {
                return true;
            }
        }

        // Check temp directory
        Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), "crm-data", fileName);
        return Files.exists(tempFilePath);
    }

}
