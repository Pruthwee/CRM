package crm.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cloud-native utility for reading data from Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class ReadDataUtils {

    private static final String BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private static final String S3_PREFIX = System.getenv().getOrDefault("S3_FILE_PREFIX", "uploads/");

    /**
     * Downloads a file from S3 to a temporary location for processing.
     * 
     * @param s3Key The S3 object key (path within bucket)
     * @param s3Client The S3 client instance
     * @return Temporary file containing the downloaded content
     * @throws IOException if download fails
     */
    public static File downloadFileFromS3(String s3Key, S3Client s3Client) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(s3Key)
                .build();

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
        
        // Create temporary file
        String fileName = s3Key.substring(s3Key.lastIndexOf('/') + 1);
        File tempFile = File.createTempFile("s3-download-", "-" + fileName);
        tempFile.deleteOnExit();
        
        // Write S3 content to temporary file
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             InputStream is = s3Object) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        return tempFile;
    }

    /**
     * Lists files in S3 bucket with specified extension filter.
     * 
     * @param fileExtension The file extension to filter (e.g., "csv", "pdf")
     * @param s3Client The S3 client instance
     * @return List of S3 object keys matching the extension
     */
    public static List<String> listFilesFromS3(String fileExtension, S3Client s3Client) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .prefix(S3_PREFIX)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        
        return listResponse.contents().stream()
                .map(S3Object::key)
                .filter(key -> key.endsWith("." + fileExtension))
                .collect(Collectors.toList());
    }

    /**
     * Gets an InputStream directly from S3 without downloading to local file.
     * Preferred for streaming operations.
     * 
     * @param s3Key The S3 object key
     * @param s3Client The S3 client instance
     * @return InputStream of the S3 object
     */
    public static InputStream getS3ObjectStream(String s3Key, S3Client s3Client) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(s3Key)
                .build();

        return s3Client.getObject(getObjectRequest);
    }
}
