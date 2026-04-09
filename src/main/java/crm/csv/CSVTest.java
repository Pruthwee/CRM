package crm.csv;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-ready CSV processor that reads from Google Cloud Storage.
 * Replaces static initialization and java.io.File usage with lazy initialization
 * and GCS operations.
 */
@Component
@Slf4j
public class CSVTest {

    @Value("${gcs.bucket.name:${GCS_BUCKET_NAME:default-bucket}}")
    private String bucketName;

    @Value("${gcs.project.id:${GCP_PROJECT_ID:}}")
    public CSVTest(Storage storage) {
        this.storage = storage;
    }

     * Validates storage client after construction.
        if (storage != null) {
        } else {
            log.warn("Storage client is null - GCS operations will fail");
        }
    }

    /**
     * Processes CSV file from Google Cloud Storage.
     * 
     * @param blobName The name/path of the CSV file in GCS
     * @return List of parsed CSV rows
     */
    public List<String[]> processCsvFromGCS(String blobName) {
        List<String[]> data = new ArrayList<>();
        
        try {
            if (storage == null) {
                log.error("Storage client not initialized");
                return data;
            }

            Blob blob = storage.get(bucketName, blobName);
            if (blob == null) {
                log.error("CSV file not found in GCS: bucket={}, blob={}", bucketName, blobName);
                return data;
            }

            byte[] content = blob.getContent();
            log.info("Reading CSV from GCS: bucket={}, blob={}, size={} bytes", 
                    bucketName, blobName, content.length);

            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(
                            new ByteArrayInputStream(content), 
                            StandardCharsets.UTF_8))) {
                
                String[] line;
                while ((line = reader.readNext()) != null) {
                    data.add(line);
                    
                    // Example processing logic
                    if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                        log.debug("Found matching row: {}", String.join(", ", line));
                    }
                }
                
                log.info("Successfully processed {} rows from CSV", data.size());
            }
        } catch (IOException e) {
            log.error("Error processing CSV from GCS: bucket={}, blob={}", bucketName, blobName, e);
        }
        
        return data;
    }

    /**
     * Processes CSV file from classpath resources (for bundled test data).
     * 
     * @param resourcePath The classpath resource path
     * @return List of parsed CSV rows
     */
    public List<String[]> processCsvFromClasspath(String resourcePath) {
        List<String[]> data = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream(resourcePath),
                        StandardCharsets.UTF_8))) {
            
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
            }
            
            log.info("Successfully processed {} rows from classpath resource: {}", data.size(), resourcePath);
        } catch (IOException e) {
            log.error("Error processing CSV from classpath: {}", resourcePath, e);
        }
        
        return data;
    }

    /**
     * Example method for testing - can be called via REST endpoint or scheduled job.
     * Replaces the static main method for cloud-friendly execution.
     */
    public void processTestCsv(String blobName) {
        log.info("Starting CSV processing for: {}", blobName);
        List<String[]> data = processCsvFromGCS(blobName);
        log.info("CSV processing completed. Total rows: {}", data.size());
    }
}
