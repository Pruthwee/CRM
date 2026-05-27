package crm.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cloud-native utility for reading data from Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class ReadDataUtils {

    private final S3Client s3Client;
    private final String bucketName;

    /**
     * Constructor with S3 client and bucket name.
     * Bucket name should be configured via environment variable (e.g., AWS_S3_BUCKET).
     * 
     * @param s3Client AWS S3 client instance
     * @param bucketName S3 bucket name for file operations
     */
    public ReadDataUtils(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * Reads a file from S3 bucket by key.
     * 
     * @param s3Key The S3 object key (path within bucket)
     * @return InputStream of the S3 object content
     */
    public InputStream readFileFromS3(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
        return s3Object;
    }

    /**
     * Lists files in S3 bucket with optional prefix filter.
     * 
     * @param prefix Optional prefix to filter objects (can be null or empty)
     * @param fileExtension Optional file extension filter (e.g., ".csv", ".pdf")
     * @return List of S3 object keys matching the criteria
     */
    public List<String> listFilesFromS3(String prefix, String fileExtension) {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName);
        
        if (prefix != null && !prefix.isEmpty()) {
            requestBuilder.prefix(prefix);
        }
        
        ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
        
        List<String> keys = response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
        
        // Filter by extension if provided
        if (fileExtension != null && !fileExtension.isEmpty()) {
            String extension = fileExtension.startsWith(".") ? fileExtension : "." + fileExtension;
            keys = keys.stream()
                    .filter(key -> key.endsWith(extension))
                    .collect(Collectors.toList());
        }
        
        return keys;
    }

    /**
     * Checks if an object exists in S3 bucket.
     * 
     * @param s3Key The S3 object key to check
     * @return true if object exists, false otherwise
     */
    public boolean fileExistsInS3(String s3Key) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(s3Key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
