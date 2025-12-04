package crm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class ReadDataUtils {

    private static final Logger log = LoggerFactory.getLogger(ReadDataUtils.class);

    /**
     * Cloud-compatible file reading using Spring MultipartFile for web-based file uploads.
     * This replaces desktop JFileChooser with web-based file upload mechanism.
     *
     * @param file MultipartFile from web upload
     * @param allowedExtensions allowed file extensions
     * @return InputStream of the uploaded file
     * @throws IOException if file reading fails
     */
    public static InputStream readFile(MultipartFile file, String... allowedExtensions) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("No file provided for upload");
            return null;
        }

        String fileName = file.getOriginalFilename();
        log.info("Processing uploaded file: {}", fileName);

        // Validate file extension
        if (allowedExtensions != null && allowedExtensions.length > 0) {
            boolean validExtension = false;
            for (String ext : allowedExtensions) {
                if (fileName != null && fileName.toLowerCase().endsWith("." + ext.toLowerCase())) {
                    validExtension = true;
                    break;
                }
            }
            if (!validExtension) {
                log.error("Invalid file extension for file: {}. Allowed: {}", fileName, String.join(", ", allowedExtensions));
                throw new IOException("Invalid file extension. Allowed extensions: " + String.join(", ", allowedExtensions));
            }
        }

        return file.getInputStream();
    }

}
