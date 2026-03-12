package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Test utility - Cloud-compatible version.
 * For cloud deployments, use REST API file uploads or cloud storage (S3, Azure Blob, GCS).
 */
public class CSVTest {

    private static final Logger log = LoggerFactory.getLogger(CSVTest.class);

    public static void main(String[] args) {
        // For cloud deployments, file path should be provided as argument or environment variable
        String csvFilePath = System.getenv("CSV_FILE_PATH");
        if (csvFilePath == null && args.length > 0) {
            csvFilePath = args[0];
        }

        if (csvFilePath == null) {
            log.error("CSV file path not provided. Use CSV_FILE_PATH environment variable or command line argument.");
            return;
        }

        File document = ReadDataUtils.ReadFile(csvFilePath, "csv");
        if (document == null) {
            log.error("Failed to read CSV file from path: {}", csvFilePath);
            return;
        }

        log.info("Processing CSV file: {}", document.getName());

        CSVReader reader = null;
        List<Object[]> data = new ArrayList<>();
        try {
            reader = new CSVReader(new FileReader(document));
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                if (line.length > 2 && "QUICK SUB".equals(line[1])) {
                    log.info("Found QUICK SUB entry: {} | {} | {}", line[0], line[1], line[2]);
                }
            }
            log.info("CSV processing completed. Total rows processed: {}", data.size());
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Error closing CSV reader: {}", e.getMessage());
                }
            }
        }
    }

}
