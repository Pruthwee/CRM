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
 * Utility class for reading data from Amazon S3 instead of local file system.
 * Replaces hard-coded file path and JFileChooser-based file selection with
 * cloud-native S3 object retrieval using AWS SDK for Java v2.
 */
public class ReadDataUtils {

    /**
     * Retrieves an S3 object as an InputStream from the specified bucket and key.
     * Replaces the local JFileChooser-based file selection with S3 object retrieval.
     *
     * @param s3Client  the AWS S3 client
     * @param bucketName the S3 bucket name (from environment variable AWS_S3_BUCKET_NAME)
     * @param objectKey  the S3 object key (path within the bucket)
     * @return InputStream of the S3 object content, or null if not found
     */
    public static InputStream readFileFromS3(S3Client s3Client, String bucketName, String objectKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            System.out.println("Reading S3 object: s3://" + bucketName + "/" + objectKey);
            return s3Object;
        } catch (Exception e) {
            System.err.println("Failed to read S3 object: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lists S3 objects in a bucket filtered by a given prefix and file extension.
     * Replaces the local JFileChooser file filter with S3 prefix-based listing.
     *
     * @param s3Client             the AWS S3 client
     * @param bucketName           the S3 bucket name
     * @param prefix               the S3 key prefix to filter objects
     * @param fileExtensionFilter  the file extension to filter (e.g., "csv")
     * @return list of matching S3 object keys
     */
    public static List<String> listFilesFromS3(S3Client s3Client, String bucketName,
                                               String prefix, String fileExtensionFilter) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        return listResponse.contents().stream()
                .map(S3Object::key)
                .filter(key -> fileExtensionFilter == null || key.endsWith("." + fileExtensionFilter))
                .collect(Collectors.toList());
    }

    /**
     * Builds a default S3Client using the default AWS credential chain.
     * Credentials are resolved from environment variables (AWS_ACCESS_KEY_ID,
     * AWS_SECRET_ACCESS_KEY, AWS_REGION) or IAM instance roles in cloud environments.
     *
     * @return configured S3Client instance
     */
    public static S3Client buildS3Client() {
        return S3Client.builder().build();
    }

}
