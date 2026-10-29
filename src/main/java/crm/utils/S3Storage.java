package crm.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Amazon S3 access for the files the application used to read from and write to the local disk.
 * The client is built lazily on first use (region and credentials come from the SDK default chain)
 * and is closed when the JVM shuts down. The bucket comes from the APP_S3_BUCKET environment variable.
 */
public final class S3Storage {

    private static final String BUCKET_ENV = "APP_S3_BUCKET";

    private static S3Client client;

    private S3Storage() {
    }

    private static synchronized S3Client client() {
        if (client == null) {
            final S3Client created = S3Client.create();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    created.close();
                }
            }));
            client = created;
        }
        return client;
    }

    private static String bucket() {
        String bucket = System.getenv(BUCKET_ENV);
        if (bucket == null || bucket.trim().isEmpty()) {
            throw SdkClientException.create(BUCKET_ENV + " is not set");
        }
        return bucket.trim();
    }

    /**
     * Writes the bytes as one object. Throws an SdkException (the SDK's base exception) when S3 cannot be used.
     */
    public static void put(String key, byte[] content, String contentType) {
        String bucket = bucket();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        client().putObject(request, RequestBody.fromBytes(content));
    }

    /**
     * Opens one object for reading; the caller closes the stream.
     * Throws an SdkException (the SDK's base exception) when S3 cannot be used.
     */
    public static ResponseInputStream<GetObjectResponse> get(String key) {
        String bucket = bucket();
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return client().getObject(request);
    }
}
