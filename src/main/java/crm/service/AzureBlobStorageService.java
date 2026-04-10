package crm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-native service for Azure Blob Storage operations.
 * Provides centralized, reactive blob storage functionality for the application.
 */
@Service
@Slf4j
public class AzureBlobStorageService {

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
                    containerClient.create();
                    log.info("Created Azure Blob Storage container: {}", containerName);
                } else {
                    log.info("Connected to Azure Blob Storage container: {}", containerName);
                }
            } catch (Exception e) {
                log.error("Failed to initialize Azure Blob Storage service", e);
            }
        } else {
            log.warn("Azure Storage connection string is not configured. Blob storage operations will not be available.");
        }
    }

    /**
     * Uploads a file to Azure Blob Storage using reactive patterns.
     *
     * @param blobName The name of the blob
     * @param data The data to upload
     * @return Mono<String> with the blob URL
     */
    public Mono<String> uploadBlob(String blobName, byte[] data) {
        return Mono.fromCallable(() -> {
            validateConfiguration();

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(new ByteArrayInputStream(data), data.length, true);

            log.info("Successfully uploaded blob: {}", blobName);
            return blobClient.getBlobUrl();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Downloads a file from Azure Blob Storage using reactive patterns.
     *
     * @param blobName The name of the blob to download
     * @return Mono<InputStream> with the blob content
     */
    public Mono<InputStream> downloadBlob(String blobName) {
        return Mono.fromCallable(() -> {
            validateConfiguration();

            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new IllegalArgumentException("Blob not found: " + blobName);
            }

            log.info("Successfully downloaded blob: {}", blobName);
            return blobClient.openInputStream();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a blob from Azure Blob Storage using reactive patterns.
     *
     * @param blobName The name of the blob to delete
     * @return Mono<Void>
     */
    public Mono<Void> deleteBlob(String blobName) {
        return Mono.fromRunnable(() -> {
            validateConfiguration();

            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (blobClient.exists()) {
                blobClient.delete();
                log.info("Successfully deleted blob: {}", blobName);
            } else {
                log.warn("Blob not found for deletion: {}", blobName);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Lists all blobs in the container using reactive patterns.
     *
     * @return Flux<String> with blob names
     */
    public Flux<String> listBlobs() {
        return Mono.fromCallable(() -> {
            validateConfiguration();

            List<String> blobNames = new ArrayList<>();
            for (BlobItem blobItem : containerClient.listBlobs()) {
                blobNames.add(blobItem.getName());
            }

            log.info("Listed {} blobs from container", blobNames.size());
            return blobNames;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    /**
     * Lists blobs with a specific prefix using reactive patterns.
     *
     * @param prefix The prefix to filter blobs
     * @return Flux<String> with blob names
     */
    public Flux<String> listBlobsWithPrefix(String prefix) {
        return Mono.fromCallable(() -> {
            validateConfiguration();

            List<String> blobNames = new ArrayList<>();
            for (BlobItem blobItem : containerClient.listBlobsByHierarchy(prefix)) {
                if (!blobItem.isPrefix()) {
                    blobNames.add(blobItem.getName());
                }
            }

            log.info("Listed {} blobs with prefix '{}' from container", blobNames.size(), prefix);
            return blobNames;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    /**
     * Checks if a blob exists in Azure Blob Storage.
     *
     * @param blobName The name of the blob to check
     * @return Mono<Boolean> indicating if the blob exists
     */
    public Mono<Boolean> blobExists(String blobName) {
        return Mono.fromCallable(() -> {
            validateConfiguration();

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.exists();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets the URL of a blob.
     *
     * @param blobName The name of the blob
     * @return Mono<String> with the blob URL
     */
    public Mono<String> getBlobUrl(String blobName) {
        return Mono.fromCallable(() -> {
            validateConfiguration();

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.getBlobUrl();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Validates that the Azure Storage configuration is available.
     *
     * @throws IllegalStateException if configuration is missing
     */
    private void validateConfiguration() {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalStateException("Azure Storage connection string is not configured. " +
                    "Please set azure.storage.connection-string in application.properties");
        }
    }

    /**
     * Checks if the service is properly configured.
     *
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return connectionString != null && !connectionString.isEmpty() && containerClient != null;
    }
}
