package crm.csv;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cloud-ready CSV processor that reads from Google Cloud Storage
 * and uses asynchronous I/O for better performance.
 */
@Slf4j
public class CSVTest {

    private static final String GCS_BUCKET_NAME = System.getenv().getOrDefault("GCS_BUCKET_NAME", "crm-data-bucket");
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Asynchronously reads and processes CSV file from Google Cloud Storage.
     * 
     * @param blobName The name/path of the CSV file in GCS
     * @return CompletableFuture containing the processed data
     */
    public static CompletableFuture<List<Object[]>> processCSVFromGCSAsync(String blobName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Storage storage = StorageOptions.getDefaultInstance().getService();
                Blob blob = storage.get(GCS_BUCKET_NAME, blobName);
                
                if (blob == null) {
                    log.error("CSV file not found in GCS: {}/{}", GCS_BUCKET_NAME, blobName);
                    return new ArrayList<>();
                }
                
                byte[] content = blob.getContent();
                log.info("Successfully retrieved CSV from GCS: {}/{}", GCS_BUCKET_NAME, blobName);
                
                return processCSVContent(content);
            } catch (Exception e) {
                log.error("Error processing CSV from GCS: {}/{}", GCS_BUCKET_NAME, blobName, e);
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * Processes CSV content from byte array.
     * 
     * @param content CSV file content as byte array
     * @return List of parsed CSV rows
     */
    private static List<Object[]> processCSVContent(byte[] content) {
        List<Object[]> data = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {
            
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                data.add(line);
                
                // Example processing logic
                if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                    log.info("Found matching record: {} {} {}", 
                            line.length > 0 ? line[0] : "", 
                            line.length > 1 ? line[1] : "", 
                            line.length > 2 ? line[2] : "");
                }
            }
            
            log.info("Successfully processed {} rows from CSV", data.size());
        } catch (Exception e) {
            log.error("Error processing CSV content", e);
        }
        
        return data;
    }

    /**
     * Synchronous version for backward compatibility.
     * 
     * @param blobName The name/path of the CSV file in GCS
     * @return List of parsed CSV rows
     */
    public static List<Object[]> processCSVFromGCS(String blobName) {
        try {
            return processCSVFromGCSAsync(blobName).get();
        } catch (Exception e) {
            log.error("Error in synchronous CSV processing", e);
            return new ArrayList<>();
        }
    }

    /**
     * Example main method demonstrating async CSV processing from GCS.
     * In production, the blob name would be passed as a parameter or configuration.
     */
    public static void main(String[] args) {
        // Example: Read CSV from GCS asynchronously
        String csvBlobName = System.getenv().getOrDefault("CSV_FILE_PATH", "csv-files/sample.csv");
        
        log.info("Starting async CSV processing from GCS: {}", csvBlobName);
        
        CompletableFuture<List<Object[]>> future = processCSVFromGCSAsync(csvBlobName);
        
        // Non-blocking: Continue with other work while CSV is being processed
        future.thenAccept(data -> {
            log.info("CSV processing completed. Total rows: {}", data.size());
            
            // Process the data
            if (!data.isEmpty() && data.size() > 1) {
                Object[] firstRow = data.get(0);
                Object[] secondRow = data.get(1);
                log.info("Sample data - First row: {}, Second row: {}", 
                        firstRow.length > 1 ? firstRow[1] : "N/A",
                        secondRow.length > 1 ? secondRow[1] : "N/A");
            }
        }).exceptionally(ex -> {
            log.error("Error in async CSV processing", ex);
            return null;
        });
        
        // Keep main thread alive for async operation to complete
        try {
            future.join();
        } catch (Exception e) {
            log.error("Error waiting for async operation", e);
        }
        
        // Shutdown executor service
        executorService.shutdown();
    }

}
