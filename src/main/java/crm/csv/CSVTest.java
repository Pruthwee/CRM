package crm.csv;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Test utility - Cloud-ready version
 * Replaced static initializer with I/O operations and File usage with InputStream
 * Removed main method and converted to Spring component for proper lifecycle management
 */
@Component
@Slf4j
public class CSVTest {

    /**
     * Process CSV data from InputStream (cloud-compatible)
     * @param inputStream InputStream of CSV file
     * @return List of parsed CSV rows
     */
    public List<String[]> processCsvData(InputStream inputStream) {
        List<String[]> data = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                
                // Example processing logic
                if (line.length > 1 && "QUICK SUB".equals(line[1])) {
                    log.info("Found QUICK SUB entry: {} {} {}", 
                            line.length > 0 ? line[0] : "", 
                            line.length > 1 ? line[1] : "", 
                            line.length > 2 ? line[2] : "");
                }
            }
            log.info("Successfully processed {} CSV rows", data.size());
        } catch (IOException e) {
            log.error("Error processing CSV data", e);
        }
        
        return data;
    }

    /**
     * Process CSV data from classpath resource
     * @param resourcePath Path to CSV resource in classpath
     * @return List of parsed CSV rows
     */
    public List<String[]> processCsvFromClasspath(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                log.error("CSV resource not found in classpath: {}", resourcePath);
                return new ArrayList<>();
            }
            return processCsvData(inputStream);
        } catch (IOException e) {
            log.error("Error reading CSV from classpath: {}", resourcePath, e);
            return new ArrayList<>();
        }
    }

    /**
     * Filter CSV data by column value
     * @param data CSV data
     * @param columnIndex Column index to filter
     * @param value Value to match
     * @return Filtered list of CSV rows
     */
    public List<String[]> filterByColumnValue(List<String[]> data, int columnIndex, String value) {
        List<String[]> filtered = new ArrayList<>();
        
        for (String[] row : data) {
            if (row.length > columnIndex && value.equals(row[columnIndex])) {
                filtered.add(row);
            }
        }
        
        log.info("Filtered {} rows matching value '{}' in column {}", filtered.size(), value, columnIndex);
        return filtered;
    }
}
