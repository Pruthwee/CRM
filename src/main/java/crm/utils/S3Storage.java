package crm.utils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * Amazon S3 object storage used in place of local files.
 * The client is built lazily on first use (default region and credentials chain),
 * so the application starts even when AWS cannot be reached.
 * Bucket name comes from the APP_S3_BUCKET environment variable.
 */
public final class S3Storage {

    public static final String BUCKET_ENV = "APP_S3_BUCKET";

    private static S3Client client;

    private S3Storage() {
    }

    private static synchronized S3Client client() {
        if (client == null) {
            client = S3Client.create();
            final S3Client created = client;
            Runtime.getRuntime().addShutdownHook(new Thread(created::close));
        }
        return client;
    }

    public static String bucket() throws IOException {
        String bucket = System.getenv(BUCKET_ENV);
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IOException(BUCKET_ENV + " is not set");
        }
        return bucket;
    }

    /** Writes the bytes as the object with the given key. Callers catch IOException and SdkException. */
    public static void put(String key, byte[] data, String contentType) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket())
                .key(key)
                .contentType(contentType)
                .build();
        client().putObject(request, RequestBody.fromBytes(data));
    }

    /** Opens the object with the given key for reading. Callers catch IOException and SdkException. */
    public static InputStream open(String key) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket())
                .key(key)
                .build();
        return client().getObject(request, ResponseTransformer.toInputStream());
    }
}
