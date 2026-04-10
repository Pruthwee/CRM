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

    @Value("${azure.storage.container-name:data-files}")
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
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            return blobClient.openInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from Azure Blob Storage: " + blobName, e);
        }
    }

    /**
     * Reads a file from classpath resources (for embedded resources).
     * 
     * @param resourcePath The classpath resource path
     * @return InputStream of the resource
     */
    public InputStream readFileFromClasspath(String resourcePath) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        return inputStream;
    }
}
