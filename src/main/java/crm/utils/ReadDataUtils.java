package crm.utils;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Cloud-ready utility for reading data from Google Cloud Storage.
 * Replaces local file system dependencies with GCS operations.
 */
@Component
public class ReadDataUtils {

    @Value("${gcs.bucket.name:${GCS_BUCKET_NAME:default-bucket}}")
    private String bucketName;

    /**
     * Reads a file from Google Cloud Storage.
     * 
     * @param blobName The name of the blob (file) in GCS
     * @return InputStream of the file content
     */
    public InputStream readFileFromGCS(String blobName) {
        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            Blob blob = storage.get(bucketName, blobName);
            
            if (blob == null) {
                throw new RuntimeException("File not found in GCS: " + blobName);
            }
            
            byte[] content = blob.getContent();
            return new ByteArrayInputStream(content);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file from GCS: " + blobName, e);
        }
    }

    /**
     * Lists files in a GCS bucket with a given prefix.
     * 
     * @param prefix The prefix to filter files
     * @return Iterable of Blob objects
     */
    public Iterable<Blob> listFilesFromGCS(String prefix) {
        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            return storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();
        } catch (Exception e) {
            throw new RuntimeException("Error listing files from GCS with prefix: " + prefix, e);
        }
    }
}
