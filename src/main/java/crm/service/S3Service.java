package crm.service;

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
 * Service for handling file uploads to Amazon S3.
 * This replaces local file system operations with cloud-native storage.
 */
@Service
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        // Initialize S3 client with default credentials provider chain
        // This supports IAM roles, environment variables, and AWS credentials file
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
        log.info("S3Service initialized with bucket: {} and region: {}", bucketName, region);
    }

    /**
     * Upload a file to S3 bucket
     * @param key The S3 object key (file name/path)
     * @param content The file content as byte array
     * @param contentType The content type (e.g., "application/pdf")
     * @return The S3 URL of the uploaded file
     */
    public String uploadFile(String key, byte[] content, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(content.length);
            metadata.setContentType(contentType);

            InputStream inputStream = new ByteArrayInputStream(content);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            
            s3Client.putObject(putObjectRequest);
            
            String s3Url = s3Client.getUrl(bucketName, key).toString();
            log.info("File uploaded successfully to S3: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * Check if a file exists in S3
     * @param key The S3 object key
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String key) {
        try {
            return s3Client.doesObjectExist(bucketName, key);
        } catch (Exception e) {
            log.error("Error checking file existence in S3: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete a file from S3
     * @param key The S3 object key
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(bucketName, key);
            log.info("File deleted successfully from S3: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }
}
