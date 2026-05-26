import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
    /**
     * Reads CSV data from Azure Blob Storage instead of the local file system.
     *
     * @param blobName name of the blob within the configured container
     * @return list of CSV rows as String[]
     */
    public static List<String[]> readCsvFromBlob(String blobName) {
        AzureBlobStorageService storageService = new AzureBlobStorageService();
        List<String[]> data = new ArrayList<>();
        try (InputStream is = storageService.openBlobInputStream(blobName);
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV from Azure Blob Storage", e);
        }
        return data;
            return chooser.getSelectedFile();
        }
        return null;
    }

}
