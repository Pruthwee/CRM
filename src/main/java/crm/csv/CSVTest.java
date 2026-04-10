package crm.csv;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cloud-ready CSV processing utility using Google Cloud Storage.
 * Implements asynchronous I/O operations for better cloud performance.
 */
@Slf4j
public class CSVTest {

    private static final String GCS_BUCKET_NAME = System.getenv().getOrDefault("GCS_BUCKET_NAME", "crm-data-bucket");
    private static final Storage storage = StorageOptions.getDefaultInstance().getService();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    /**
     * Read CSV file from Google Cloud Storage asynchronously
     * @param blobName The name/path of the CSV file in GCS
     * @return CompletableFuture containing list of CSV rows
     */
    public static CompletableFuture<List<String[]>> readCsvFromGcsAsync(String blobName) {
        return CompletableFuture.supplyAsync(() -> {
            List<String[]> data = new ArrayList<>();
            CSVReader reader = null;
            
            try {
                log.info("Reading CSV from GCS: {}/{}", GCS_BUCKET_NAME, blobName);
                
                Blob blob = storage.get(GCS_BUCKET_NAME, blobName);
                if (blob == null) {
                    log.error("CSV file not found in GCS: {}/{}", GCS_BUCKET_NAME, blobName);
                    return data;
                }
                
                byte[] content = blob.getContent();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
                reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                
                String[] line;
                int rowCount = 0;
                while ((line = reader.readNext()) != null) {
                    data.add(line);
                    rowCount++;
                    
                    // Example: Filter specific data
                    if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                        log.info("Found matching row: {} - {} - {}", 
                                line.length > 0 ? line[0] : "", 
                                line[1], 
                                line.length > 2 ? line[2] : "");
                    }
                }
                
                log.info("Successfully read {} rows from CSV: {}", rowCount, blobName);
                
            } catch (IOException e) {
                log.error("Error reading CSV from GCS: {}/{}", GCS_BUCKET_NAME, blobName, e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("Error closing CSV reader", e);
                    }
                }
            }
            
            return data;
        }, executorService);
    }

    /**
     * Process CSV data asynchronously with filtering
     * @param data List of CSV rows
     * @param filterColumn Column index to filter
     * @param filterValue Value to match
     * @return CompletableFuture containing filtered rows
     */
    public static CompletableFuture<List<String[]>> filterCsvDataAsync(
            List<String[]> data, int filterColumn, String filterValue) {
        return CompletableFuture.supplyAsync(() -> {
            List<String[]> filteredData = new ArrayList<>();
            
            for (String[] row : data) {
                if (row.length > filterColumn && filterValue.equals(row[filterColumn])) {
                    filteredData.add(row);
                }
            }
            
            log.info("Filtered {} rows matching '{}' in column {}", 
                    filteredData.size(), filterValue, filterColumn);
            
            return filteredData;
        }, executorService);
    }

    /**
     * List all CSV files in GCS bucket with given prefix
     * @param prefix Prefix to filter blobs (e.g., "csv/")
     * @return List of blob names
     */
    public static List<String> listCsvFilesInGcs(String prefix) {
        List<String> csvFiles = new ArrayList<>();
        
        try {
            Iterable<Blob> blobs = storage.list(GCS_BUCKET_NAME, 
                    Storage.BlobListOption.prefix(prefix)).iterateAll();
            
            for (Blob blob : blobs) {
                if (blob.getName().endsWith(".csv")) {
                    csvFiles.add(blob.getName());
                }
            }
            
            log.info("Found {} CSV files in GCS with prefix: {}", csvFiles.size(), prefix);
            
        } catch (Exception e) {
            log.error("Error listing CSV files in GCS", e);
        }
        
        return csvFiles;
    }

    /**
     * Example main method demonstrating cloud-ready CSV processing
     */
    public static void main(String[] args) {
        try {
            // Example: Read CSV from GCS environment variable or default
            String csvBlobName = System.getenv().getOrDefault("CSV_FILE_PATH", "csv/sample.csv");
            
            log.info("Starting CSV processing from GCS: {}", csvBlobName);
            
            // Asynchronously read CSV
            CompletableFuture<List<String[]>> futureData = readCsvFromGcsAsync(csvBlobName);
            
            // Process data when available
            futureData.thenAccept(data -> {
                log.info("CSV processing completed. Total rows: {}", data.size());
                
                // Example: Filter data asynchronously
                if (!data.isEmpty()) {
                    filterCsvDataAsync(data, 1, "QUICK SUB")
                            .thenAccept(filteredData -> {
                                log.info("Filtering completed. Matching rows: {}", filteredData.size());
                            });
                }
            }).exceptionally(ex -> {
                log.error("Error during CSV processing", ex);
                return null;
            });
            
            // Wait for completion (in real application, use proper async handling)
            Thread.sleep(5000);
            
        } catch (Exception e) {
            log.error("Error in CSV processing", e);
        } finally {
            executorService.shutdown();
        }
    }
}
