package crm.csv;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-ready CSV processing utility using Azure Blob Storage and reactive patterns.
 * Replaces java.io.File operations with Azure Blob Storage and synchronous blocking with reactive streams.
 */
@Slf4j
public class CSVTest {

    private static final String CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final String CONTAINER_NAME = System.getenv("AZURE_STORAGE_CONTAINER_NAME") != null 
            ? System.getenv("AZURE_STORAGE_CONTAINER_NAME") 
            : "crm-files";

    /**
     * Reads CSV file from Azure Blob Storage using reactive patterns.
     * 
     * @param blobName The name of the CSV blob to read
     * @return Flux of String arrays representing CSV rows
     */
    public static Flux<String[]> readCsvFromBlobStorage(String blobName) {
        return Mono.fromCallable(() -> {
            if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
                throw new IllegalStateException("Azure Storage connection string is not configured. " +
                        "Please set AZURE_STORAGE_CONNECTION_STRING environment variable");
            }

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(CONNECTION_STRING)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            
            if (!containerClient.exists()) {
                throw new IllegalStateException("Container does not exist: " + CONTAINER_NAME);
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                throw new IllegalArgumentException("Blob not found: " + blobName);
            }

            return blobClient.openInputStream();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(inputStream -> {
            List<String[]> rows = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    rows.add(line);
                }
            } catch (IOException e) {
                log.error("Error reading CSV from blob storage", e);
                throw new RuntimeException("Failed to read CSV from blob storage", e);
            }
            return Flux.fromIterable(rows);
        });
    }

    /**
     * Filters CSV rows based on a specific column value using reactive patterns.
     * 
     * @param blobName The name of the CSV blob to read
     * @param columnIndex The column index to filter on
     * @param filterValue The value to filter for
     * @return Flux of filtered String arrays
     */
    public static Flux<String[]> filterCsvRows(String blobName, int columnIndex, String filterValue) {
        return readCsvFromBlobStorage(blobName)
                .filter(line -> line.length > columnIndex && line[columnIndex].equals(filterValue))
                .doOnNext(line -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < line.length; i++) {
                        sb.append(line[i]);
                        if (i < line.length - 1) {
                            sb.append("\t");
                        }
                    }
                    log.info("Filtered row: {}", sb.toString());
                });
    }

    /**
     * Lists all CSV files in the Azure Blob Storage container.
     * 
     * @return Flux of blob names
     */
    public static Flux<String> listCsvBlobs() {
        return Mono.fromCallable(() -> {
            if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
                throw new IllegalStateException("Azure Storage connection string is not configured. " +
                        "Please set AZURE_STORAGE_CONNECTION_STRING environment variable");
            }

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(CONNECTION_STRING)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            
            List<String> csvBlobs = new ArrayList<>();
            containerClient.listBlobs().forEach(blobItem -> {
                if (blobItem.getName().endsWith(".csv")) {
                    csvBlobs.add(blobItem.getName());
                }
            });
            
            return csvBlobs;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    /**
     * Main method demonstrating reactive CSV processing from Azure Blob Storage.
     * 
     * @param args Command line arguments (expects blob name as first argument)
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            log.error("Usage: CSVTest <blob-name>");
            log.info("Listing available CSV files in Azure Blob Storage...");
            
            listCsvBlobs()
                    .doOnNext(blobName -> log.info("Available CSV: {}", blobName))
                    .doOnComplete(() -> log.info("Listing complete"))
                    .doOnError(error -> log.error("Error listing CSV files", error))
                    .blockLast();
            
            return;
        }

        String blobName = args[0];
        log.info("Processing CSV file from Azure Blob Storage: {}", blobName);

        // Example: Filter rows where column 1 equals "QUICK SUB"
        filterCsvRows(blobName, 1, "QUICK SUB")
                .doOnNext(line -> {
                    if (line.length >= 3) {
                        log.info("Matched row: {} \t {} \t {}", line[0], line[1], line[2]);
                    }
                })
                .doOnComplete(() -> log.info("CSV processing complete"))
                .doOnError(error -> log.error("Error processing CSV", error))
                .blockLast();
    }
}
