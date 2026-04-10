package crm.csv;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-ready CSV processing service using Azure Blob Storage.
 * Replaces static initialization and local file system dependencies.
 */
@Service
public class CSVTest {

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.container-name:csv-files}")
    private String containerName;

    /**
     * Lazy initialization using Spring's lifecycle hooks.
     * Replaces static initializer to avoid startup failures in cloud environments.
     */
    @PostConstruct
    public void init() {
        // Initialization logic can be placed here if needed
        // This ensures resources are loaded at runtime, not during class loading
    }

    /**
     * Reads CSV data from Azure Blob Storage.
     * 
     * @param blobName The name of the CSV blob to read
     * @return List of CSV rows as Object arrays
     */
    public List<Object[]> readCsvFromBlobStorage(String blobName) {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalStateException("Azure Storage connection string is not configured. " +
                    "Please set azure.storage.connection-string in application.properties");
        }

        List<Object[]> data = new ArrayList<>();
        
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            try (InputStream inputStream = blobClient.openInputStream();
                 InputStreamReader reader = new InputStreamReader(inputStream);
                 CSVReader csvReader = new CSVReader(reader)) {

                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    data.add(line);
                    
                    // Example processing logic
                    if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                        System.out.println(line[0] + "\t" + line[1] + "\t" + (line.length > 2 ? line[2] : ""));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV from Azure Blob Storage: " + blobName, e);
        }

        return data;
    }

    /**
     * Reads CSV data from classpath resources (for embedded CSV files).
     * 
     * @param resourcePath The classpath resource path
     * @return List of CSV rows as Object arrays
     */
    public List<Object[]> readCsvFromClasspath(String resourcePath) {
        List<Object[]> data = new ArrayList<>();
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
             InputStreamReader reader = new InputStreamReader(inputStream);
             CSVReader csvReader = new CSVReader(reader)) {

            if (inputStream == null) {
                throw new IllegalArgumentException("CSV resource not found: " + resourcePath);
            }

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                data.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV from classpath: " + resourcePath, e);
        }

        return data;
    }

    /**
     * Example main method for testing (should be replaced with proper unit tests).
     * This method is no longer using static initialization with I/O operations.
     */
    public static void main(String[] args) {
        System.out.println("CSVTest service should be used as a Spring bean, not as a standalone application.");
        System.out.println("Please inject this service into your controllers or other services.");
    }
}
