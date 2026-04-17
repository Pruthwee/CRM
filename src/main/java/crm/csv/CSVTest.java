package crm.csv;

import com.opencsv.CSVReader;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Cloud-native CSV processing using Amazon S3 with asynchronous I/O.
 * Replaces local file system dependencies and synchronous blocking operations.
 */
public class CSVTest {

    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private static final String S3_CSV_KEY = System.getenv().getOrDefault("S3_CSV_KEY", "uploads/data.csv");

    /**
     * Asynchronously reads CSV data from S3 and processes it.
     * Uses CompletableFuture for non-blocking I/O operations.
     */
    public static CompletableFuture<List<Object[]>> readCsvFromS3Async(S3AsyncClient s3AsyncClient, String s3Key) {
        // Create temporary file path for download
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), "csv-" + System.currentTimeMillis() + ".csv");
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(S3_BUCKET_NAME)
                .key(s3Key)
                .build();

        // Asynchronously download from S3
        CompletableFuture<GetObjectResponse> downloadFuture = s3AsyncClient.getObject(
                getObjectRequest,
                AsyncResponseTransformer.toFile(tempFile)
        );

        // Chain CSV processing after download completes
        return downloadFuture.thenApplyAsync(response -> {
            List<Object[]> data = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new BufferedReader(
                    new InputStreamReader(java.nio.file.Files.newInputStream(tempFile))))) {
                
                String[] line;
                while ((line = reader.readNext()) != null) {
                    data.add(line);
                    if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                        System.out.println(line[0] + "\t" + line[1] + "\t" + (line.length > 2 ? line[2] : ""));
                    }
                }
                
                // Clean up temporary file
                java.nio.file.Files.deleteIfExists(tempFile);
                
            } catch (IOException e) {
                System.err.println("Error reading CSV from S3: " + e.getMessage());
                e.printStackTrace();
            }
            return data;
        }).exceptionally(throwable -> {
            System.err.println("Error downloading CSV from S3: " + throwable.getMessage());
            throwable.printStackTrace();
            return new ArrayList<>();
        });
    }

    /**
     * Synchronous wrapper for backward compatibility.
     * Prefer using the async version for better cloud performance.
     */
    public static List<Object[]> readCsvFromS3(S3AsyncClient s3AsyncClient, String s3Key) {
        try {
            return readCsvFromS3Async(s3AsyncClient, s3Key).get();
        } catch (Exception e) {
            System.err.println("Error in synchronous CSV read: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        // Example usage with S3AsyncClient
        // S3AsyncClient should be injected or created with proper AWS credentials
        System.out.println("CSVTest now uses Amazon S3 with asynchronous I/O.");
        System.out.println("Configure S3_BUCKET_NAME and S3_CSV_KEY environment variables.");
        System.out.println("Use readCsvFromS3Async() method with S3AsyncClient for cloud-native CSV processing.");
    }

}
