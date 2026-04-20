package crm.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Service for handling Amazon S3 operations.
 * Replaces local file system operations with cloud-native S3 storage.
 */
@Service
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket.name:crm-pdf-storage}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        try {
            // Use DefaultAWSCredentialsProviderChain for cloud-native credential management
            // This supports IAM roles, environment variables, and credential files
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .build();
            log.info("S3 client initialized successfully for bucket: {} in region: {}", bucketName, region);
        } catch (Exception e) {
            log.error("Failed to initialize S3 client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize S3 client", e);
        }
    }

    /**
     * Upload a byte array to S3 bucket.
     *
     * @param key The S3 object key (file name)
     * @param data The byte array data to upload
     * @param contentType The content type of the file
     * @return The S3 URL of the uploaded file
     */
    public String uploadFile(String key, byte[] data, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setContentType(contentType);

            InputStream inputStream = new ByteArrayInputStream(data);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            
            s3Client.putObject(putObjectRequest);
            
            String s3Url = s3Client.getUrl(bucketName, key).toString();
            log.info("File uploaded successfully to S3: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Upload an input stream to S3 bucket.
     *
     * @param key The S3 object key (file name)
     * @param inputStream The input stream to upload
     * @param contentLength The content length
     * @param contentType The content type of the file
     * @return The S3 URL of the uploaded file
     */
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            metadata.setContentType(contentType);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            
            s3Client.putObject(putObjectRequest);
            
            String s3Url = s3Client.getUrl(bucketName, key).toString();
            log.info("File uploaded successfully to S3: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Delete a file from S3 bucket.
     *
     * @param key The S3 object key (file name)
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(bucketName, key);
            log.info("File deleted successfully from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    /**
     * Check if a file exists in S3 bucket.
     *
     * @param key The S3 object key (file name)
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String key) {
        try {
            return s3Client.doesObjectExist(bucketName, key);
        } catch (Exception e) {
            log.error("Failed to check if file exists in S3: {}", e.getMessage(), e);
            return false;
        }
    }
}
