package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Test utility that reads CSV files from Amazon S3.
 * Migrated from local file system to cloud-native S3 storage.
 */
public class CSVTest {

    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private static final String AWS_REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");
    private static final String S3_CSV_KEY = System.getenv().getOrDefault("S3_CSV_KEY", "data/sample.csv");

    public static void main(String[] args) {
        // Use S3 key from environment variable or default
        String s3Key = (args.length > 0) ? args[0] : S3_CSV_KEY;
        
        System.out.println("Reading CSV file from S3: " + S3_BUCKET_NAME + "/" + s3Key);
        
        // Read CSV directly from S3 using InputStream (cloud-native approach)
        readCsvFromS3(S3_BUCKET_NAME, s3Key);
    }

    /**
     * Reads CSV file directly from Amazon S3 using InputStream.
     * This is a cloud-native approach that doesn't rely on local file system.
     * 
     * @param bucketName The S3 bucket name
     * @param s3Key The S3 object key (path to CSV file)
     */
    private static void readCsvFromS3(String bucketName, String s3Key) {
        S3Client s3Client = null;
        CSVReader reader = null;
        List<Object[]> data = new ArrayList<>();
        
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

            // Get the S3 object as an InputStream
            ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest);
            
            // Create CSVReader directly from S3 InputStream (no temporary file needed)
            reader = new CSVReader(new InputStreamReader(s3InputStream));
            
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                
                // Process specific records
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
                }
            }
            
            System.out.println("Successfully processed " + data.size() + " rows from S3 CSV file");
            
        } catch (S3Exception e) {
            System.err.println("S3 Error: " + e.awsErrorDetails().errorMessage());
            System.err.println("Bucket: " + bucketName + ", Key: " + s3Key);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO Error reading CSV from S3: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing CSV reader: " + e.getMessage());
                }
            }
            if (s3Client != null) {
                s3Client.close();
            }
        }
    }

    /**
     * Alternative method using ReadDataUtils for backward compatibility.
     * Downloads file to temporary location first.
     * 
     * @param s3Key The S3 object key
     */
    @Deprecated
    private static void readCsvUsingUtils(String s3Key) {
        java.io.File document = ReadDataUtils.ReadFile(s3Key, null, "Only CSV Files", "csv");
        
        if (document == null) {
            System.err.println("Failed to download file from S3");
            return;
        }

        CSVReader reader = null;
        List<Object[]> data = new ArrayList<>();
        try {
            reader = new CSVReader(new java.io.FileReader(document));
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing CSV reader: " + e.getMessage());
                }
            }
        }
    }
}
