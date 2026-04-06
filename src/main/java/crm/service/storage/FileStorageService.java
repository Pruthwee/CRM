package crm.service.storage;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Cloud-ready file storage service for AWS S3.
 * 
 * This service provides abstraction for file storage operations,
 * making it easy to switch between local storage (for development)
 * and AWS S3 (for production cloud deployment).
 * 
 * Configuration (via environment variables):
 * - FILE_STORAGE_TYPE: "local" or "s3" (default: "local")
 * - AWS_REGION: AWS region for S3 (default: "us-east-1")
 * - S3_BUCKET_NAME: S3 bucket name for file storage
 * 
 * AWS Credentials:
 * - Use IAM roles for EC2/ECS/EKS (recommended)
 * - Or set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables
 * - Or use AWS credentials file (~/.aws/credentials)
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.storage.type:local}")
    private String storageType;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.s3.bucket:crm-files-bucket}")
    private String s3BucketName;

    private AmazonS3 s3Client;

    /**
     * Initialize S3 client if storage type is S3.
     */
    private AmazonS3 getS3Client() {
        if (s3Client == null && "s3".equalsIgnoreCase(storageType)) {
            logger.info("Initializing AWS S3 client for region: {}", awsRegion);
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(awsRegion)
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .build();
        }
        return s3Client;
    }

    /**
     * Upload a file to storage (local or S3 based on configuration).
     * 
     * @param file File to upload
     * @param key Storage key/path for the file
     * @return Storage location identifier
     * @throws IOException if upload fails
     */
    public String uploadFile(File file, String key) throws IOException {
        if ("s3".equalsIgnoreCase(storageType)) {
            return uploadToS3(file, key);
        } else {
            return uploadToLocal(file, key);
        }
    }

    /**
     * Download a file from storage (local or S3 based on configuration).
     * 
     * @param key Storage key/path for the file
     * @return File object pointing to the downloaded file
     * @throws IOException if download fails
     */
    public File downloadFile(String key) throws IOException {
        if ("s3".equalsIgnoreCase(storageType)) {
            return downloadFromS3(key);
        } else {
            return downloadFromLocal(key);
        }
    }

    /**
     * Upload file to AWS S3.
     */
    private String uploadToS3(File file, String key) throws IOException {
        try {
            logger.info("Uploading file to S3: bucket={}, key={}", s3BucketName, key);
            
            AmazonS3 s3 = getS3Client();
            PutObjectRequest request = new PutObjectRequest(s3BucketName, key, file);
            s3.putObject(request);
            
            String s3Url = String.format("s3://%s/%s", s3BucketName, key);
            logger.info("Successfully uploaded file to S3: {}", s3Url);
            return s3Url;
            
        } catch (Exception e) {
            logger.error("Failed to upload file to S3: bucket={}, key={}", s3BucketName, key, e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    /**
     * Download file from AWS S3.
     */
    private File downloadFromS3(String key) throws IOException {
        try {
            logger.info("Downloading file from S3: bucket={}, key={}", s3BucketName, key);
            
            AmazonS3 s3 = getS3Client();
            S3Object s3Object = s3.getObject(new GetObjectRequest(s3BucketName, key));
            
            // Create temp file
            String fileName = key.substring(key.lastIndexOf('/') + 1);
            Path tempFile = Files.createTempFile("s3-download-", "-" + fileName);
            
            // Copy S3 object content to temp file
            try (InputStream inputStream = s3Object.getObjectContent()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            File file = tempFile.toFile();
            file.deleteOnExit();
            
            logger.info("Successfully downloaded file from S3: {}", key);
            return file;
            
        } catch (Exception e) {
            logger.error("Failed to download file from S3: bucket={}, key={}", s3BucketName, key, e);
            throw new IOException("Failed to download file from S3", e);
        }
    }

    /**
     * Upload file to local storage (for development).
     */
    private String uploadToLocal(File file, String key) throws IOException {
        logger.info("Uploading file to local storage: key={}", key);
        
        Path uploadDir = Files.createTempDirectory("crm-uploads");
        Path targetPath = uploadDir.resolve(key);
        
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Successfully uploaded file to local storage: {}", targetPath);
        return targetPath.toString();
    }

    /**
     * Download file from local storage (for development).
     */
    private File downloadFromLocal(String key) throws IOException {
        logger.info("Downloading file from local storage: key={}", key);
        
        File file = new File(key);
        if (!file.exists()) {
            throw new IOException("File not found in local storage: " + key);
        }
        
        logger.info("Successfully retrieved file from local storage: {}", key);
        return file;
    }

    /**
     * Delete a file from storage.
     * 
     * @param key Storage key/path for the file
     * @throws IOException if deletion fails
     */
    public void deleteFile(String key) throws IOException {
        if ("s3".equalsIgnoreCase(storageType)) {
            deleteFromS3(key);
        } else {
            deleteFromLocal(key);
        }
    }

    /**
     * Delete file from AWS S3.
     */
    private void deleteFromS3(String key) throws IOException {
        try {
            logger.info("Deleting file from S3: bucket={}, key={}", s3BucketName, key);
            
            AmazonS3 s3 = getS3Client();
            s3.deleteObject(s3BucketName, key);
            
            logger.info("Successfully deleted file from S3: {}", key);
            
        } catch (Exception e) {
            logger.error("Failed to delete file from S3: bucket={}, key={}", s3BucketName, key, e);
            throw new IOException("Failed to delete file from S3", e);
        }
    }

    /**
     * Delete file from local storage.
     */
    private void deleteFromLocal(String key) throws IOException {
        logger.info("Deleting file from local storage: key={}", key);
        
        File file = new File(key);
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to delete file from local storage: " + key);
        }
        
        logger.info("Successfully deleted file from local storage: {}", key);
    }
}
