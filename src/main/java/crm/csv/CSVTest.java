package crm.csv;

import com.opencsv.CSVReader;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSVTest demonstrates reading CSV data from Amazon S3 instead of the local file system.
 * Replaces java.io.File-based persistent storage operations (blocker cr-java-0063)
 * with Amazon S3 client calls using AWS SDK for Java v2 to achieve cloud-native,
 * durable, and scalable storage without host-level file system dependencies.
 *
 * S3 bucket and object key are resolved from environment variables:
 *   AWS_S3_BUCKET_NAME  - the S3 bucket containing CSV files
 *   AWS_S3_CSV_KEY      - the S3 object key of the CSV file to process
 */
public class CSVTest {

    public static void main(String[] args) {
        // Resolve S3 bucket and object key from environment variables (12-factor app principle)
        String bucketName = System.getenv("AWS_S3_BUCKET_NAME") != null
                ? System.getenv("AWS_S3_BUCKET_NAME") : "crm-data-storage";
        String csvObjectKey = System.getenv("AWS_S3_CSV_KEY") != null
                ? System.getenv("AWS_S3_CSV_KEY") : "csv/data.csv";

        // Build S3 client using default AWS credential chain (env vars, IAM role, etc.)
        S3Client s3Client = S3Client.builder().build();

        // Retrieve the CSV file directly from Amazon S3 (replaces java.io.File usage)
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(csvObjectKey)
                .build();

        System.out.println("Reading CSV from S3: s3://" + bucketName + "/" + csvObjectKey);

        List<Object[]> data = new ArrayList<>();
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
             InputStreamReader inputStreamReader = new InputStreamReader(s3Object, StandardCharsets.UTF_8);
             CSVReader reader = new CSVReader(inputStreamReader)) {

            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
