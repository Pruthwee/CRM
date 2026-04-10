package crm.utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Cloud-ready utility for reading data from Azure Blob Storage.
 * Replaces local file system dependencies with Azure Blob Storage.
 */
@Component
public class ReadDataUtils {

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.container-name:crm-files}")
    private String containerName;

    /**
     * Reads a file from Azure Blob Storage.
     * 
     * @param blobName The name of the blob to read
     * @return InputStream of the blob content
     */
    public InputStream readFileFromBlobStorage(String blobName) {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalStateException("Azure Storage connection string is not configured. " +
                    "Please set azure.storage.connection-string in application.properties");
        }

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            // Create container if it doesn't exist
            if (!containerClient.exists()) {
                containerClient.create();
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                throw new IllegalArgumentException("Blob not found: " + blobName);
            }

            return blobClient.openInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from Azure Blob Storage: " + blobName, e);
        }
    }

    /**
     * Lists all blobs in the container with a specific extension.
     * 
     * @param fileExtension The file extension to filter (e.g., "csv", "pdf")
     * @return Iterable of blob names
     */
    public Iterable<String> listBlobsByExtension(String fileExtension) {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalStateException("Azure Storage connection string is not configured. " +
                    "Please set azure.storage.connection-string in application.properties");
        }

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            return containerClient.listBlobsByHierarchy("/")
                    .stream()
                    .filter(item -> !item.isPrefix() && item.getName().endsWith("." + fileExtension))
                    .map(item -> item.getName())
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list blobs from Azure Blob Storage", e);
        }
    }

    /**
     * Checks if a blob exists in Azure Blob Storage.
     * 
     * @param blobName The name of the blob to check
     * @return true if the blob exists, false otherwise
     */
    public boolean blobExists(String blobName) {
        if (connectionString == null || connectionString.isEmpty()) {
            return false;
        }

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            return blobClient.exists();
        } catch (Exception e) {
            return false;
        }
    }
}
