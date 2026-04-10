package crm.utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ReadDataUtils.class);

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
        try {
            if (connectionString == null || connectionString.isEmpty()) {
                logger.warn("Azure Storage connection string not configured. Using empty stream.");
                return new ByteArrayInputStream(new byte[0]);
            }

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            // Create container if it doesn't exist
            if (!containerClient.exists()) {
                logger.info("Container {} does not exist. Creating...", containerName);
                containerClient.create();
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                logger.warn("Blob {} does not exist in container {}", blobName, containerName);
                return new ByteArrayInputStream(new byte[0]);
            }

            logger.info("Successfully retrieved blob: {} from container: {}", blobName, containerName);
            return blobClient.openInputStream();
            
        } catch (Exception e) {
            logger.error("Error reading file from Azure Blob Storage: {}", e.getMessage(), e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    /**
     * Lists all blobs in the container with a specific extension.
     * 
     * @param fileExtension The file extension to filter (e.g., "csv", "pdf")
     * @return Iterable of blob names
     */
    public Iterable<String> listBlobsByExtension(String fileExtension) {
        try {
            if (connectionString == null || connectionString.isEmpty()) {
                logger.warn("Azure Storage connection string not configured.");
                return java.util.Collections.emptyList();
            }

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            if (!containerClient.exists()) {
                logger.warn("Container {} does not exist", containerName);
                return java.util.Collections.emptyList();
            }

            return containerClient.listBlobs().stream()
                    .filter(blob -> blob.getName().endsWith("." + fileExtension))
                    .map(blob -> blob.getName())
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error listing blobs from Azure Blob Storage: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Checks if a blob exists in the container.
     * 
     * @param blobName The name of the blob to check
     * @return true if blob exists, false otherwise
     */
    public boolean blobExists(String blobName) {
        try {
            if (connectionString == null || connectionString.isEmpty()) {
                return false;
            }

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            if (!containerClient.exists()) {
                return false;
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.exists();
            
        } catch (Exception e) {
            logger.error("Error checking blob existence: {}", e.getMessage(), e);
            return false;
        }
    }
}
