package crm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
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
 * Utility class for reading data files from Amazon S3.
 *
 * <p>Replaces the previous local-filesystem / Swing JFileChooser implementation
 * with cloud-native Amazon S3 object storage access using AWS SDK for Java v2.
 * The S3 bucket name and AWS region are resolved from environment variables
 * (AWS_S3_BUCKET_NAME, AWS_REGION) so that no file-system paths are hard-coded
 * and the application remains fully portable across cloud environments.</p>
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code AWS_S3_BUCKET_NAME} – name of the S3 bucket that holds the data files</li>
 *   <li>{@code AWS_REGION}         – AWS region where the bucket resides (e.g. {@code us-east-1})</li>
 * </ul>
 * AWS credentials are resolved automatically via the
 * <a href="https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials.html">
 * Default Credential Provider Chain</a> (IAM role, environment variables, ~/.aws/credentials, etc.).
 * </p>
 */
public class ReadDataUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReadDataUtils.class);

    /** Environment variable that holds the S3 bucket name. */
    private static final String ENV_BUCKET = "AWS_S3_BUCKET_NAME";

    /** Environment variable that holds the AWS region. */
    private static final String ENV_REGION = "AWS_REGION";

    // -----------------------------------------------------------------------
    // S3 client (lazily initialised, shared across calls)
    // -----------------------------------------------------------------------

    private static volatile S3Client s3Client;

    private static S3Client getS3Client() {
        if (s3Client == null) {
            synchronized (ReadDataUtils.class) {
                if (s3Client == null) {
                    String regionName = System.getenv(ENV_REGION);
                    Region region = (regionName != null && !regionName.isEmpty())
                            ? Region.of(regionName)
                            : Region.US_EAST_1;
                    s3Client = S3Client.builder()
                            .region(region)
                            .build();
                    logger.info("S3Client initialised for region '{}'", region);
                }
            }
        }
        return s3Client;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Opens an {@link InputStream} for the S3 object identified by {@code s3Key}.
     *
     * <p>This method replaces the former {@code ReadFile()} method that used a
     * Swing {@code JFileChooser} to select a local file.  Callers should close
     * the returned stream when they are finished with it.</p>
     *
     * @param s3Key the S3 object key (path within the bucket) of the file to read
     * @return an {@link InputStream} over the object's content, or {@code null}
     *         if the key is blank or the object cannot be retrieved
     */
    public static InputStream readFileFromS3(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            logger.warn("readFileFromS3 called with a blank key – returning null");
            return null;
        }

        String bucketName = resolveBucketName();
        if (bucketName == null) {
            return null;
        }

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = getS3Client().getObject(request);
            logger.info("Successfully opened S3 object: s3://{}/{}", bucketName, s3Key);
            return response;
        } catch (Exception e) {
            logger.error("Failed to read S3 object s3://{}/{}: {}", bucketName, s3Key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lists S3 object keys in the configured bucket whose names end with one of
     * the supplied file extensions.
     *
     * <p>This is a cloud-native replacement for the file-extension filtering that
     * was previously performed by {@code FileNameExtensionFilter} inside the
     * Swing {@code JFileChooser}.</p>
     *
     * @param prefix         optional S3 key prefix (folder path); pass {@code ""} to list the whole bucket
     * @param fileExtensions one or more file extensions to filter by (e.g. {@code "csv"}, {@code "pdf"})
     * @return a list of matching S3 object keys; never {@code null}
     */
    public static List<String> listFilesFromS3(String prefix, String... fileExtensions) {
        String bucketName = resolveBucketName();
        if (bucketName == null) {
            return List.of();
        }

        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName);
            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Response response = getS3Client().listObjectsV2(requestBuilder.build());

            List<String> keys = response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> matchesExtension(key, fileExtensions))
                    .collect(Collectors.toList());

            logger.info("Listed {} matching objects in s3://{}/{}", keys.size(), bucketName,
                    prefix != null ? prefix : "");
            return keys;
        } catch (Exception e) {
            logger.error("Failed to list S3 objects in bucket '{}': {}", bucketName, e.getMessage(), e);
            return List.of();
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String resolveBucketName() {
        String bucketName = System.getenv(ENV_BUCKET);
        if (bucketName == null || bucketName.trim().isEmpty()) {
            logger.error("Environment variable '{}' is not set. Cannot access S3.", ENV_BUCKET);
            return null;
        }
        return bucketName.trim();
    }

    private static boolean matchesExtension(String key, String[] extensions) {
        if (extensions == null || extensions.length == 0) {
            return true;
        }
        String lowerKey = key.toLowerCase();
        for (String ext : extensions) {
            if (lowerKey.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
