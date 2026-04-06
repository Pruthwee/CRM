package crm.csv;

import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-ready CSV processing component.
 * Reads CSV files from classpath resources or cloud storage instead of local file system.
 * Uses Spring lifecycle management with @PostConstruct for proper initialization.
 */
@Component
public class CSVTest {

    private static final Logger log = LoggerFactory.getLogger(CSVTest.class);

    @Value("${csv.default.resource:data/sample.csv}")
    private String defaultCsvResource;

    @Value("${csv.processing.enabled:false}")
    private boolean processingEnabled;

    /**
     * Initialize CSV processing after Spring context is ready.
     * Uses @PostConstruct instead of static initializer for proper error handling.
     */
    @PostConstruct
    public void init() {
        if (processingEnabled) {
            log.info("CSV processing is enabled, initializing...");
            try {
                processDefaultCsv();
            } catch (Exception e) {
                log.error("Failed to initialize CSV processing: {}", e.getMessage(), e);
                // Don't fail application startup, just log the error
            }
        } else {
            log.info("CSV processing is disabled");
        }
    }

    /**
     * Process CSV from classpath resource instead of file system.
     * This approach works in containerized and cloud environments.
     */
    private void processDefaultCsv() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(defaultCsvResource)) {
            if (inputStream == null) {
                log.warn("CSV resource not found: {}", defaultCsvResource);
                return;
            }
            
            List<String[]> data = readCsvFromStream(inputStream);
            log.info("Successfully processed {} rows from CSV", data.size());
            
            // Process data
            for (String[] line : data) {
                if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                    log.debug("Found matching record: {} {} {}", 
                        line.length > 0 ? line[0] : "", 
                        line.length > 1 ? line[1] : "", 
                        line.length > 2 ? line[2] : "");
                }
            }
        } catch (IOException e) {
            log.error("Error processing CSV: {}", e.getMessage(), e);
        }
    }

    /**
     * Read CSV data from input stream (cloud-compatible approach).
     * 
     * @param inputStream Input stream containing CSV data
     * @return List of CSV rows
     * @throws IOException if reading fails
     */
    public List<String[]> readCsvFromStream(InputStream inputStream) throws IOException {
        List<String[]> data = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
            }
        }
        
        return data;
    }

    /**
     * Process CSV data from byte array (useful for Azure Blob Storage integration).
     * 
     * @param csvBytes CSV file content as bytes
     * @return List of CSV rows
     */
    public List<String[]> processCsvFromBytes(byte[] csvBytes) {
        try (InputStream inputStream = new java.io.ByteArrayInputStream(csvBytes)) {
            return readCsvFromStream(inputStream);
        } catch (IOException e) {
            log.error("Error processing CSV from bytes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

}
