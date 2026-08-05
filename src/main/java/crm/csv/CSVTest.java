package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSVTest – reads a CSV file from Amazon S3 and processes its contents.
 *
 * <p>Migrated from {@code java.io.File} / local-filesystem access to
 * cloud-native Amazon S3 storage using {@link ReadDataUtils#readFileFromS3(String)}.
 * The S3 object key is resolved from the {@code CSV_S3_KEY} environment variable
 * so that no file-system paths are hard-coded in the application.</p>
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code AWS_S3_BUCKET_NAME} – S3 bucket that holds the CSV files</li>
 *   <li>{@code AWS_REGION}         – AWS region of the bucket (e.g. {@code us-east-1})</li>
 *   <li>{@code CSV_S3_KEY}         – S3 object key of the CSV file to process</li>
 * </ul>
 * </p>
 */
public class CSVTest {

    public static void main(String[] args) {
        // Resolve the S3 object key from an environment variable instead of
        // using java.io.File / JFileChooser for local file selection.
        String s3Key = System.getenv("CSV_S3_KEY");
        if (s3Key == null || s3Key.trim().isEmpty()) {
            System.err.println("Environment variable 'CSV_S3_KEY' is not set. Cannot locate CSV file in S3.");
            return;
        }

        // Obtain an InputStream directly from S3 – no local File object required.
        InputStream csvStream = ReadDataUtils.readFileFromS3(s3Key.trim());
        if (csvStream == null) {
            System.err.println("Failed to retrieve CSV file from S3 for key: " + s3Key);
            return;
        }

        CSVReader reader;
        List<Object[]> data = new ArrayList<>();
        try {
            // Use InputStreamReader over the S3 InputStream instead of FileReader
            // over a local java.io.File, eliminating the host file-system dependency.
            reader = new CSVReader(new InputStreamReader(csvStream));
            String[] line;
            while ((line = reader.readNext()) != null) {
//                System.out.println(line[1] + "\t" + line[2]);
                data.add(line);
                if (line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                csvStream.close();
            } catch (IOException ignored) {
                // best-effort close
            }
        }
		/*System.out.println(data.get(0)[1] + "\t" + data.get(0)[2]);
		System.out.println(data.get(1)[1] + "\t" + data.get(1)[2]);*/
    }

}
