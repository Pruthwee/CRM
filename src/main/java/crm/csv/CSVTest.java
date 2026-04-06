package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Test utility - Cloud-ready version
 * Removed static initializers with I/O operations and File usage.
 * Now uses classpath resources for cloud compatibility.
 */
@Slf4j
public class CSVTest {

    /**
     * Process CSV file from classpath resources.
     * This method replaces the static main method with I/O operations.
     * 
     * @param resourcePath the path to CSV file in classpath (e.g., "data/sample.csv")
     * @return List of parsed CSV data
     * @throws IOException if file cannot be read
     */
    public static List<String[]> processCsvFromClasspath(String resourcePath) throws IOException {
        log.info("Processing CSV file from classpath: {}", resourcePath);
        
        List<String[]> data = new ArrayList<>();
        
        try (InputStream inputStream = ReadDataUtils.readFileFromClasspath(resourcePath);
             InputStreamReader reader = new InputStreamReader(inputStream);
             CSVReader csvReader = new CSVReader(reader)) {
            
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                data.add(line);
                // Example processing - can be customized
                if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                    log.debug("Found matching row: {} {} {}", 
                        line.length > 0 ? line[0] : "", 
                        line.length > 1 ? line[1] : "", 
                        line.length > 2 ? line[2] : "");
                }
            }
            
            log.info("Successfully processed {} rows from CSV", data.size());
            return data;
            
        } catch (IOException e) {
            log.error("Failed to process CSV file: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Process CSV data from InputStream.
     * Useful for processing uploaded files or data from cloud storage.
     * 
     * @param inputStream the input stream containing CSV data
     * @return List of parsed CSV data
     * @throws IOException if stream cannot be read
     */
    public static List<String[]> processCsvFromStream(InputStream inputStream) throws IOException {
        log.info("Processing CSV from input stream");
        
        List<String[]> data = new ArrayList<>();
        
        try (InputStreamReader reader = new InputStreamReader(inputStream);
             CSVReader csvReader = new CSVReader(reader)) {
            
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                data.add(line);
            }
            
            log.info("Successfully processed {} rows from CSV stream", data.size());
            return data;
            
        } catch (IOException e) {
            log.error("Failed to process CSV stream: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Example usage method (non-static, can be called from Spring context)
     * Replaces the static main method for better cloud compatibility
     */
    public void processExample(String resourcePath) {
        try {
            List<String[]> data = processCsvFromClasspath(resourcePath);
            log.info("Processed {} rows from CSV file", data.size());
            
            // Example: Print first two rows if available
            if (data.size() > 0) {
                String[] firstRow = data.get(0);
                log.debug("First row: {} columns", firstRow.length);
            }
            if (data.size() > 1) {
                String[] secondRow = data.get(1);
                log.debug("Second row: {} columns", secondRow.length);
            }
        } catch (IOException e) {
            log.error("Error processing CSV example: {}", e.getMessage(), e);
        }
    }
}
