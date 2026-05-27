package crm.csv;

import com.opencsv.CSVReader;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-native CSV processing using Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class CSVTest {

    public static void main(String[] args) {
        // Configuration from environment variables for cloud deployment
        String bucketName = System.getenv().getOrDefault("AWS_S3_BUCKET", "default-csv-bucket");
        String s3Key = System.getenv().getOrDefault("CSV_S3_KEY", "data/sample.csv");
        String awsRegion = System.getenv().getOrDefault("AWS_REGION", "us-east-1");
        
        // Initialize S3 client with default credentials provider (uses IAM roles in cloud)
        S3Client s3Client = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        
        try {
            // Read CSV file from S3
            List<Object[]> data = readCsvFromS3(s3Client, bucketName, s3Key);
            
            // Process CSV data
            for (Object[] line : data) {
                String[] row = (String[]) line;
                if (row.length > 1 && "QUICK SUB".equals(row[1])) {
                    System.out.println(row[0] + "\t" + row[1] + "\t" + (row.length > 2 ? row[2] : ""));
                }
            }
            
            System.out.println("Successfully processed " + data.size() + " rows from S3");
            
        } catch (IOException e) {
            System.err.println("Error reading CSV from S3: " + e.getMessage());
            e.printStackTrace();
        } finally {
            s3Client.close();
        }
    }

    /**
     * Reads CSV file from Amazon S3 and returns parsed data.
     * 
     * @param s3Client AWS S3 client instance
     * @param bucketName S3 bucket name
     * @param s3Key S3 object key (path to CSV file)
     * @return List of CSV rows as Object arrays
     * @throws IOException if reading fails
     */
    public static List<Object[]> readCsvFromS3(S3Client s3Client, String bucketName, String s3Key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        
        ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
        
        CSVReader reader = null;
        List<Object[]> data = new ArrayList<>();
        
        try {
            reader = new CSVReader(new InputStreamReader(s3ObjectStream));
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            s3ObjectStream.close();
        }
        
        return data;
    }
}
