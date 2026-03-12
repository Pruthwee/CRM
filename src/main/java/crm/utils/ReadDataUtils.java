package crm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cloud-compatible file reading utility.
 * Replaced JFileChooser (desktop UI component) with path-based file access.
 * For cloud deployments, files should be uploaded via REST API or read from cloud storage.
 */
public class ReadDataUtils {

    private static final Logger log = LoggerFactory.getLogger(ReadDataUtils.class);

    /**
     * Read file from specified path.
     * In cloud environments, use this with file uploads or cloud storage paths.
     *
     * @param filePath Path to the file to read
     * @param fileExtension Expected file extensions for validation
     * @return File object if exists and valid, null otherwise
     * @deprecated Use multipart file upload in REST controllers for cloud deployments
     */
    @Deprecated
    public static File ReadFile(String filePath, String... fileExtension) {
        if (filePath == null || filePath.isEmpty()) {
            log.warn("File path is null or empty");
            return null;
        }

        Path path = Paths.get(filePath);
        File file = path.toFile();

        if (!file.exists()) {
            log.warn("File does not exist: {}", filePath);
            return null;
        }

        if (!file.isFile()) {
            log.warn("Path is not a file: {}", filePath);
            return null;
        }

        // Validate file extension if provided
        if (fileExtension != null && fileExtension.length > 0) {
            String fileName = file.getName();
            boolean validExtension = false;
            for (String ext : fileExtension) {
                if (fileName.toLowerCase().endsWith("." + ext.toLowerCase())) {
                    validExtension = true;
                    break;
                }
            }
            if (!validExtension) {
                log.warn("File does not have valid extension. Expected: {}, Got: {}",
                    String.join(", ", fileExtension), fileName);
                return null;
            }
        }

        log.info("File validated successfully: {}", file.getName());
        return file;
    }

}
