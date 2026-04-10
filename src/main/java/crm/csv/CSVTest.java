package crm.csv;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Cloud-ready CSV processing utility using Azure Blob Storage and reactive patterns.
 * Replaces local file system operations with cloud storage and synchronous blocking with async operations.
 */
public class CSVTest {

    private static final Logger logger = LoggerFactory.getLogger(CSVTest.class);

    /**
     * Processes CSV files from Azure Blob Storage using reactive patterns.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER_NAME");
        String blobName = System.getenv("CSV_BLOB_NAME");

        if (connectionString == null || containerName == null || blobName == null) {
            logger.warn("Azure Storage configuration not found in environment variables.");
            logger.info("Required environment variables:");
            logger.info("  AZURE_STORAGE_CONNECTION_STRING");
            logger.info("  AZURE_STORAGE_CONTAINER_NAME");
            logger.info("  CSV_BLOB_NAME");
            return;
        }

        // Process CSV asynchronously using reactive patterns
        processCSVFromAzureAsync(connectionString, containerName, blobName)
                .subscribe(
                        data -> logger.info("Successfully processed {} rows", data.size()),
                        error -> logger.error("Error processing CSV: {}", error.getMessage(), error),
                        () -> logger.info("CSV processing completed")
                );

        // Keep main thread alive for async processing
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Processes CSV from Azure Blob Storage using reactive Mono pattern.
     * 
     * @param connectionString Azure Storage connection string
     * @param containerName Container name
     * @param blobName Blob name
     * @return Mono containing list of CSV rows
     */
    public static Mono<List<Object[]>> processCSVFromAzureAsync(String connectionString, 
                                                                  String containerName, 
                                                                  String blobName) {
        return Mono.fromCallable(() -> {
            try {
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
                
                if (!containerClient.exists()) {
                    logger.error("Container {} does not exist", containerName);
                    return new ArrayList<Object[]>();
                }

                BlobClient blobClient = containerClient.getBlobClient(blobName);
                
                if (!blobClient.exists()) {
                    logger.error("Blob {} does not exist in container {}", blobName, containerName);
                    return new ArrayList<Object[]>();
                }

                // Download and process CSV
                InputStream inputStream = blobClient.openInputStream();
                return processCSVStream(inputStream);
                
            } catch (Exception e) {
                logger.error("Error processing CSV from Azure Blob Storage: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process CSV", e);
            }
        });
    }

    /**
     * Processes CSV stream and returns data as list.
     * 
     * @param inputStream Input stream of CSV data
     * @return List of CSV rows
     */
    private static List<Object[]> processCSVStream(InputStream inputStream) {
        List<Object[]> data = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                
                // Example: Filter specific rows
                if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                    logger.info("Found matching row: {} {} {}", 
                            line.length > 0 ? line[0] : "", 
                            line.length > 1 ? line[1] : "", 
                            line.length > 2 ? line[2] : "");
                }
            }
            logger.info("Successfully processed {} rows from CSV", data.size());
        } catch (Exception e) {
            logger.error("Error reading CSV stream: {}", e.getMessage(), e);
        }
        
        return data;
    }

    /**
     * Processes CSV using reactive Flux for streaming large files.
     * 
     * @param connectionString Azure Storage connection string
     * @param containerName Container name
     * @param blobName Blob name
     * @return Flux of CSV rows
     */
    public static Flux<String[]> processCSVStreamReactive(String connectionString, 
                                                           String containerName, 
                                                           String blobName) {
        return Flux.create(sink -> {
            try {
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
                BlobClient blobClient = containerClient.getBlobClient(blobName);
                
                InputStream inputStream = blobClient.openInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] values = line.split(",");
                    sink.next(values);
                }
                
                bufferedReader.close();
                sink.complete();
                
            } catch (Exception e) {
                logger.error("Error in reactive CSV processing: {}", e.getMessage(), e);
                sink.error(e);
            }
        });
    }

    /**
     * Asynchronous CSV processing using CompletableFuture.
     * 
     * @param connectionString Azure Storage connection string
     * @param containerName Container name
     * @param blobName Blob name
     * @return CompletableFuture containing list of CSV rows
     */
    public static CompletableFuture<List<Object[]>> processCSVAsync(String connectionString, 
                                                                      String containerName, 
                                                                      String blobName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

                BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
                BlobClient blobClient = containerClient.getBlobClient(blobName);
                
                InputStream inputStream = blobClient.openInputStream();
                return processCSVStream(inputStream);
                
            } catch (Exception e) {
                logger.error("Error in async CSV processing: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process CSV asynchronously", e);
            }
        });
    }
}
