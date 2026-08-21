package crm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Service for managing file storage in Amazon S3.
 * Replaces local file system operations with cloud-native S3 storage.
 */
@Service
@Slf4j
public class S3StorageService {

    @Value("${aws.s3.bucket.name:${S3_BUCKET_NAME:crm-data-bucket}}")
    private String bucketName;

    @Value("${aws.region:${AWS_REGION:us-east-1}}")
    private String awsRegion;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        try {
            // Initialize S3 client with default credentials provider
            // This will use IAM roles in AWS environments or environment variables
            s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            log.info("S3 client initialized successfully for bucket: {} in region: {}", bucketName, awsRegion);
        } catch (Exception e) {
            log.error("Failed to initialize S3 client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize S3 storage service", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (s3Client != null) {
            s3Client.close();
            log.info("S3 client closed successfully");
        }
    }

    /**
     * Upload a file to S3 bucket
     *
     * @param key         The S3 object key (file path in bucket)
     * @param inputStream The input stream containing file data
     * @param contentType The content type of the file
     * @param contentLength The size of the content in bytes
     * @return The S3 URL of the uploaded file
     */
    public String uploadFile(String key, InputStream inputStream, String contentType, long contentLength) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            
            String s3Url = String.format("s3://%s/%s", bucketName, key);
            log.info("File uploaded successfully to S3: {}", s3Url);
            return s3Url;
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload a byte array to S3 bucket
     *
     * @param key         The S3 object key (file path in bucket)
     * @param data        The byte array containing file data
     * @param contentType The content type of the file
     * @return The S3 URL of the uploaded file
     */
    public String uploadFile(String key, byte[] data, String contentType) {
        return uploadFile(key, new ByteArrayInputStream(data), contentType, data.length);
    }

    /**
     * Get the S3 bucket name
     *
     * @return The configured S3 bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Get the AWS region
     *
     * @return The configured AWS region
     */
    public String getAwsRegion() {
        return awsRegion;
    }
}
