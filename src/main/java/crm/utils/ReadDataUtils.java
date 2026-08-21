package crm.utils;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for reading files from Amazon S3.
 * Replaces local file system dependencies with cloud-native S3 storage.
 */
public class ReadDataUtils {

    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private static final String AWS_REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

    /**
     * Downloads a file from Amazon S3 to a temporary local file.
     * 
     * @param s3Key The S3 object key (path) of the file to download
     * @param fileExtension The expected file extension for validation
     * @return File object pointing to the downloaded temporary file, or null if download fails
     */
    public static File ReadFile(String s3Key, String fileExtension) {
        return downloadFromS3(s3Key, fileExtension);
    }

    /**
     * Legacy method signature maintained for backward compatibility.
     * Now uses S3 instead of JFileChooser.
     * 
     * @param s3Key The S3 object key (replaces dialogMessage parameter)
     * @param bucketName The S3 bucket name (replaces parent parameter, can be null to use default)
     * @param fileExtensionDescription Description (ignored, kept for compatibility)
     * @param fileExtension Expected file extensions for validation
     * @return File object pointing to the downloaded temporary file, or null if download fails
     */
    public static File ReadFile(String s3Key, String bucketName, String fileExtensionDescription,
                                String... fileExtension) {
        String bucket = (bucketName != null && !bucketName.isEmpty()) ? bucketName : S3_BUCKET_NAME;
        String extension = (fileExtension != null && fileExtension.length > 0) ? fileExtension[0] : "csv";
        return downloadFromS3(bucket, s3Key, extension);
    }

    /**
     * Downloads a file from the default S3 bucket.
     * 
     * @param s3Key The S3 object key
     * @param fileExtension The expected file extension
     * @return File object or null if download fails
     */
    private static File downloadFromS3(String s3Key, String fileExtension) {
        return downloadFromS3(S3_BUCKET_NAME, s3Key, fileExtension);
    }

    /**
     * Downloads a file from the specified S3 bucket to a temporary local file.
     * 
     * @param bucketName The S3 bucket name
     * @param s3Key The S3 object key
     * @param fileExtension The expected file extension
     * @return File object pointing to the downloaded temporary file, or null if download fails
     */
    private static File downloadFromS3(String bucketName, String s3Key, String fileExtension) {
        S3Client s3Client = null;
        File tempFile = null;
        
        try {
            // Initialize S3 client with default credentials provider (uses IAM roles in cloud)
            s3Client = S3Client.builder()
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            // Create GetObject request
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // Download the file from S3
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            // Create a temporary file
            String fileName = s3Key.substring(s3Key.lastIndexOf('/') + 1);
            String prefix = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            String suffix = "." + fileExtension;
            tempFile = File.createTempFile(prefix, suffix);
            tempFile.deleteOnExit(); // Ensure cleanup

            // Write S3 content to temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 InputStream is = s3Object) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("Successfully downloaded file from S3: " + bucketName + "/" + s3Key);
            System.out.println("Temporary file created at: " + tempFile.getAbsolutePath());
            
            return tempFile;

        } catch (S3Exception e) {
            System.err.println("S3 Error downloading file: " + e.awsErrorDetails().errorMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("IO Error creating temporary file: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (s3Client != null) {
                s3Client.close();
            }
        }
    }

    /**
     * Gets an InputStream directly from S3 without creating a temporary file.
     * More efficient for streaming operations.
     * 
     * @param bucketName The S3 bucket name
     * @param s3Key The S3 object key
     * @return InputStream from S3 object, or null if download fails
     */
    public static InputStream getS3InputStream(String bucketName, String s3Key) {
        try {
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (S3Exception e) {
            System.err.println("S3 Error: " + e.awsErrorDetails().errorMessage());
            e.printStackTrace();
            return null;
        }
    }
}
