package crm.utils;

import software.amazon.awssdk.core.exception.SdkException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ReadDataUtils {

    /** Environment variable holding the S3 key of the file to read (the bucket is APP_S3_BUCKET). */
    public static final String KEY_ENV = "APP_S3_KEY";

    /**
     * Reads the file from Amazon S3 (bucket APP_S3_BUCKET, key APP_S3_KEY) instead of asking
     * for a path on the local disk. The object is copied to a temporary file under java.io.tmpdir,
     * so callers read the returned File as before. Returns null when the file cannot be read.
     */
    public static File ReadFile(String dialogMEssage, JFrame parent, String fileExtensionDescription,
                                String... fileExtension) {
        try {
            String key = System.getenv(KEY_ENV);
            if (key == null || key.trim().isEmpty()) {
                throw new IOException(KEY_ENV + " is not set");
            }
            String name = key.substring(key.lastIndexOf('/') + 1);
            String suffix = name.contains(".") ? name.substring(name.lastIndexOf('.')) : ".tmp";
            File tmp = File.createTempFile("s3-", suffix);
            tmp.deleteOnExit();
            try (InputStream in = S3Storage.open(key)) {
                Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("You chose to open this file: " + name);
            return tmp;
        } catch (IOException | SdkException e) {
            System.out.println("Could not read file from S3: " + e.getMessage());
        }
        return null;
    }

}
