package crm.csv;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-ready CSV processor that reads from Google Cloud Storage.
 * Replaces local file system dependencies with GCS operations.
 * Uses lazy initialization instead of static initializers.
 */
@Component
public class CSVTest {

    @Value("${gcs.bucket.name:${GCS_BUCKET_NAME:default-bucket}}")
    private String bucketName;

    /**
     * Processes CSV file from Google Cloud Storage.
     * 
     * @param blobName The name of the CSV file in GCS
     * @return List of parsed CSV rows
     */
    public List<String[]> processCSVFromGCS(String blobName) {
        List<String[]> data = new ArrayList<>();
        
        try {
            // Read from Google Cloud Storage
            Storage storage = StorageOptions.getDefaultInstance().getService();
            Blob blob = storage.get(bucketName, blobName);
            
            if (blob == null) {
                throw new RuntimeException("CSV file not found in GCS: " + blobName);
            }
            
            byte[] content = blob.getContent();
            
            // Parse CSV content
            try (CSVReader reader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(content)))) {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    data.add(line);
                    
                    // Example processing logic
                    if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                        System.out.println(line[0] + "\t" + line[1] + "\t" + (line.length > 2 ? line[2] : ""));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing CSV from GCS: " + blobName, e);
        }
        
        return data;
    }

    /**
     * Processes CSV file from classpath resources (for bundled data).
     * 
     * @param resourcePath The classpath resource path
     * @return List of parsed CSV rows
     */
    public List<String[]> processCSVFromClasspath(String resourcePath) {
        List<String[]> data = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourcePath)))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing CSV from classpath: " + resourcePath, e);
        }
        
        return data;
    }

    /**
     * Main method for testing - uses environment variable for GCS blob name.
     */
    public static void main(String[] args) {
        CSVTest csvTest = new CSVTest();
        csvTest.bucketName = System.getenv("GCS_BUCKET_NAME");
        
        String csvBlobName = System.getenv("CSV_BLOB_NAME");
        if (csvBlobName == null || csvBlobName.isEmpty()) {
            System.err.println("CSV_BLOB_NAME environment variable not set");
            return;
        }
        
        try {
            List<String[]> data = csvTest.processCSVFromGCS(csvBlobName);
            System.out.println("Successfully processed " + data.size() + " rows from GCS");
        } catch (Exception e) {
            System.err.println("Error processing CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
