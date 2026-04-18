package crm.csv;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cloud-ready CSV processing utility.
 * Implements hybrid storage strategy with local caching and async processing.
 * Uses GCS for durable storage with local ephemeral disk caching for performance.
 */
@Slf4j
public class CSVTest {

    private static final String GCS_BUCKET_NAME_ENV = "GCS_BUCKET_NAME";
    private static final String CSV_FILE_PATH_ENV = "CSV_FILE_PATH";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * Downloads CSV file from GCS to local cache for processing.
     * Implements hybrid storage strategy for read-heavy workloads.
     * 
     * @param bucketName GCS bucket name
     * @param objectName Object path in GCS
     * @return Path to cached local file
     */
    private static Path downloadFromGCS(String bucketName, String objectName) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(bucketName, objectName);
        
        if (blob == null) {
            throw new FileNotFoundException("CSV file not found in GCS: " + objectName);
        }
        
        // Create temporary file for local caching (ephemeral storage)
        Path tempFile = Files.createTempFile("csv-cache-", ".csv");
        tempFile.toFile().deleteOnExit();
        
        // Download from GCS to local cache
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            blob.downloadTo(fos);
        }
        
        log.info("CSV file cached locally from GCS: bucket={}, object={}, localPath={}, size={} bytes",
                 bucketName, objectName, tempFile.toString(), blob.getSize());
        
        return tempFile;
    }

    /**
     * Processes CSV file asynchronously using Cloud Tasks pattern.
     * Replaces synchronous blocking I/O with async processing.
     * 
     * @param csvFile Path to CSV file
     * @param filterValue Value to filter on column 1
     * @return CompletableFuture with filtered results
     */
    private static CompletableFuture<List<Object[]>> processCSVAsync(Path csvFile, String filterValue) {
        return CompletableFuture.supplyAsync(() -> {
            List<Object[]> data = new ArrayList<>();
            List<Object[]> filteredData = new ArrayList<>();
            
            try (CSVReader reader = new CSVReader(new FileReader(csvFile.toFile()))) {
                String[] line;
                int lineCount = 0;
                
                while ((line = reader.readNext()) != null) {
                    data.add(line);
                    lineCount++;
                    
                    // Apply filter if specified
                    if (filterValue != null && line.length > 1 && filterValue.equals(line[1])) {
                        filteredData.add(line);
                        log.debug("Filtered row: {} | {} | {}", 
                                 line.length > 0 ? line[0] : "", 
                                 line.length > 1 ? line[1] : "", 
                                 line.length > 2 ? line[2] : "");
                    }
                }
                
                log.info("CSV processing completed: totalRows={}, filteredRows={}", lineCount, filteredData.size());
                
            } catch (IOException e) {
                log.error("Error processing CSV file: {}", csvFile, e);
                throw new UncheckedIOException("Failed to process CSV file", e);
            }
            
            return filterValue != null ? filteredData : data;
            
        }, executorService);
    }

    /**
     * Main method demonstrating cloud-native CSV processing.
     * Uses environment variables for configuration and async processing.
     */
    public static void main(String[] args) {
        String bucketName = System.getenv(GCS_BUCKET_NAME_ENV);
        String csvFilePath = System.getenv(CSV_FILE_PATH_ENV);
        
        if (bucketName == null || bucketName.isEmpty()) {
            log.error("GCS_BUCKET_NAME environment variable is not set");
            System.err.println("Error: GCS_BUCKET_NAME environment variable must be configured");
            System.exit(1);
        }
        
        if (csvFilePath == null || csvFilePath.isEmpty()) {
            log.error("CSV_FILE_PATH environment variable is not set");
            System.err.println("Error: CSV_FILE_PATH environment variable must be configured");
            System.exit(1);
        }
        
        try {
            // Download CSV from GCS to local cache (hybrid storage strategy)
            Path cachedFile = downloadFromGCS(bucketName, csvFilePath);
            
            // Process CSV asynchronously (non-blocking I/O)
            CompletableFuture<List<Object[]>> futureResults = processCSVAsync(cachedFile, "QUICK SUB");
            
            // Handle results asynchronously
            futureResults.thenAccept(results -> {
                log.info("CSV processing completed successfully: resultCount={}", results.size());
                
                // Print filtered results
                results.forEach(row -> {
                    if (row.length >= 3) {
                        System.out.println(row[0] + "\t" + row[1] + "\t" + row[2]);
                    }
                });
                
            }).exceptionally(ex -> {
                log.error("CSV processing failed", ex);
                return null;
            }).join(); // Wait for completion in main method
            
        } catch (IOException e) {
            log.error("Failed to download CSV from GCS", e);
            System.err.println("Error: Failed to download CSV from GCS - " + e.getMessage());
            System.exit(1);
        } finally {
            executorService.shutdown();
        }
    }

}
