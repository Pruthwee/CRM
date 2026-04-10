package crm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Azure Blob Storage Service for cloud-native file operations.
 * Provides centralized access to Azure Blob Storage for the application.
 */
@Service
public class AzureBlobStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageService.class);

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.container-name:crm-files}")
    private String containerName;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isEmpty()) {
            try {
                blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

                containerClient = blobServiceClient.getBlobContainerClient(containerName);

                // Create container if it doesn't exist
                if (!containerClient.exists()) {
                    logger.info("Creating Azure Blob Storage container: {}", containerName);
                    containerClient.create();
                }

                logger.info("Azure Blob Storage Service initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Azure Blob Storage Service: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("Azure Storage connection string not configured. File operations will be limited.");
        }
    }

    /**
     * Uploads a file to Azure Blob Storage.
     *
     * @param blobName The name of the blob
     * @param data The data to upload
     * @return true if successful, false otherwise
     */
    public boolean uploadBlob(String blobName, byte[] data) {
        if (!isConfigured()) {
            logger.warn("Azure Storage not configured. Cannot upload blob: {}", blobName);
            return false;
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            blobClient.upload(inputStream, data.length, true);
            logger.info("Successfully uploaded blob: {}", blobName);
            return true;
        } catch (Exception e) {
            logger.error("Error uploading blob {}: {}", blobName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Uploads a file to Azure Blob Storage asynchronously.
     *
     * @param blobName The name of the blob
     * @param data The data to upload
     * @return Mono<Boolean> indicating success
     */
    public Mono<Boolean> uploadBlobAsync(String blobName, byte[] data) {
        return Mono.fromCallable(() -> uploadBlob(blobName, data));
    }

    /**
     * Downloads a blob from Azure Blob Storage.
     *
     * @param blobName The name of the blob
     * @return InputStream of the blob content
     */
    public InputStream downloadBlob(String blobName) {
        if (!isConfigured()) {
            logger.warn("Azure Storage not configured. Cannot download blob: {}", blobName);
            return new ByteArrayInputStream(new byte[0]);
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (!blobClient.exists()) {
                logger.warn("Blob {} does not exist", blobName);
                return new ByteArrayInputStream(new byte[0]);
            }
            logger.info("Successfully downloaded blob: {}", blobName);
            return blobClient.openInputStream();
        } catch (Exception e) {
            logger.error("Error downloading blob {}: {}", blobName, e.getMessage(), e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    /**
     * Downloads a blob asynchronously.
     *
     * @param blobName The name of the blob
     * @return Mono<InputStream> of the blob content
     */
    public Mono<InputStream> downloadBlobAsync(String blobName) {
        return Mono.fromCallable(() -> downloadBlob(blobName));
    }

    /**
     * Checks if a blob exists.
     *
     * @param blobName The name of the blob
     * @return true if blob exists, false otherwise
     */
    public boolean blobExists(String blobName) {
        if (!isConfigured()) {
            return false;
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.exists();
        } catch (Exception e) {
            logger.error("Error checking blob existence {}: {}", blobName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lists all blobs in the container.
     *
     * @return List of blob names
     */
    public List<String> listBlobs() {
        if (!isConfigured()) {
            logger.warn("Azure Storage not configured. Cannot list blobs.");
            return new ArrayList<>();
        }

        try {
            return containerClient.listBlobs().stream()
                    .map(BlobItem::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error listing blobs: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Lists all blobs with a specific extension.
     *
     * @param extension The file extension (e.g., "pdf", "csv")
     * @return List of blob names
     */
    public List<String> listBlobsByExtension(String extension) {
        if (!isConfigured()) {
            logger.warn("Azure Storage not configured. Cannot list blobs.");
            return new ArrayList<>();
        }

        try {
            return containerClient.listBlobs().stream()
                    .map(BlobItem::getName)
                    .filter(name -> name.endsWith("." + extension))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error listing blobs by extension: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Lists blobs reactively using Flux.
     *
     * @return Flux of blob names
     */
    public Flux<String> listBlobsReactive() {
        if (!isConfigured()) {
            return Flux.empty();
        }

        return Flux.create(sink -> {
            try {
                containerClient.listBlobs().forEach(blob -> sink.next(blob.getName()));
                sink.complete();
            } catch (Exception e) {
                logger.error("Error in reactive blob listing: {}", e.getMessage(), e);
                sink.error(e);
            }
        });
    }

    /**
     * Deletes a blob from Azure Blob Storage.
     *
     * @param blobName The name of the blob to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteBlob(String blobName) {
        if (!isConfigured()) {
            logger.warn("Azure Storage not configured. Cannot delete blob: {}", blobName);
            return false;
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (blobClient.exists()) {
                blobClient.delete();
                logger.info("Successfully deleted blob: {}", blobName);
                return true;
            } else {
                logger.warn("Blob {} does not exist", blobName);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting blob {}: {}", blobName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if Azure Storage is properly configured.
     *
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return connectionString != null && !connectionString.isEmpty() && containerClient != null;
    }

    /**
     * Gets the container name.
     *
     * @return Container name
     */
    public String getContainerName() {
        return containerName;
    }
}
