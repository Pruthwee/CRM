package crm.utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.io.InputStream;
import java.net.URI;

/**
 * Simple Azure Blob Storage helper for reading and writing blobs.
 * Configuration is provided via environment variables to follow 12-factor principles.
 */
public class AzureBlobStorageService {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageService() {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER");
        if (connectionString == null || containerName == null) {
            throw new IllegalStateException("Azure Storage configuration missing. Please set AZURE_STORAGE_CONNECTION_STRING and AZURE_STORAGE_CONTAINER environment variables.");
        }
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();
    }

    public InputStream openBlobInputStream(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return blobClient.openInputStream();
    }

    public void uploadFromStream(String blobName, InputStream data, long length) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(data, length, true);
    }

    public URI getBlobUri(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return blobClient.getBlobUrl() != null ? URI.create(blobClient.getBlobUrl()) : null;
    }
}
