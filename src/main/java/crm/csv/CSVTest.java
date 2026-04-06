package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Test utility - Cloud-ready implementation.
 * 
 * This class demonstrates how to read CSV files in cloud environments.
 * Instead of using desktop file chooser dialogs, files should be:
 * 1. Loaded from classpath resources (packaged with application)
 * 2. Uploaded via REST API endpoints
 * 3. Retrieved from cloud storage (AWS S3)
 * 
 * Usage in cloud:
 * - Place test CSV files in src/main/resources/data/ directory
 * - Reference them using classpath: "data/sample.csv"
 */
public class CSVTest {

    private static final Logger logger = LoggerFactory.getLogger(CSVTest.class);

    public static void main(String[] args) {
        // Cloud-ready approach: Load from classpath resources
        String resourcePath = System.getenv().getOrDefault("CSV_TEST_FILE", "data/sample.csv");
        
        try {
            logger.info("Loading CSV file from classpath: {}", resourcePath);
            File document = ReadDataUtils.readFileFromClasspath(resourcePath);
            
            processCSVFile(document);
            
        } catch (IOException e) {
            logger.error("Failed to load CSV file from classpath: {}", resourcePath, e);
            logger.info("To use this test class in cloud environments:");
            logger.info("1. Place your CSV file in src/main/resources/data/");
            logger.info("2. Set CSV_TEST_FILE environment variable to the resource path");
            logger.info("3. Or implement a REST endpoint for file upload");
        }
    }
    
    /**
     * Process CSV file and extract data.
     * This method is cloud-compatible as it works with File objects
     * regardless of their source (classpath, temp upload, S3, etc.)
     */
    private static void processCSVFile(File document) {
        logger.info("Processing CSV file: {}", document.getName());
        
        CSVReader reader;
        List<Object[]> data = new ArrayList<>();
        
        try {
            reader = new CSVReader(new FileReader(document));
            String[] line;
            
            while ((line = reader.readNext()) != null) {
                data.add(line);
                
                // Example: Filter specific records
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    logger.info("Found matching record: {} | {} | {}", 
                               line[0], line[1], line.length > 2 ? line[2] : "N/A");
                }
            }
            
            reader.close();
            logger.info("Successfully processed {} rows from CSV file", data.size());
            
        } catch (IOException e) {
            logger.error("Error processing CSV file", e);
        }
    }
}
