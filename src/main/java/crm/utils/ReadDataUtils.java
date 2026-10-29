package crm.utils;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ReadDataUtils {

    private static final String KEY_ENV = "CSV_S3_KEY";

    /**
     * Opens the input object in Amazon S3 (bucket from APP_S3_BUCKET, object key from CSV_S3_KEY)
     * in place of the file chosen in a local file dialog. The caller closes the stream.
     * Returns null when the object cannot be read, as the dialog returned null when nothing was chosen.
     */
    public static InputStream ReadStream(String dialogMEssage, JFrame parent, String fileExtensionDescription,
                                         String... fileExtension) {
        try {
            String key = System.getenv(KEY_ENV);
            if (key == null || key.trim().isEmpty()) {
                throw SdkClientException.create(KEY_ENV + " is not set");
            }
            InputStream stream = S3Storage.get(key.trim());
            System.out.println("You chose to open this object: " + key.trim());
            return stream;
        } catch (SdkException e) {
            System.out.println("Could not open the object in S3: " + e.getMessage());
            return null;
        }
    }

    public static File ReadFile(String dialogMEssage, JFrame parent, String fileExtensionDescription,
                                String... fileExtension) {
        InputStream stream = ReadStream(dialogMEssage, parent, fileExtensionDescription, fileExtension);
        if (stream == null) {
            return null;
        }
        try {
            File copy = File.createTempFile("s3-input-", ".csv");
            copy.deleteOnExit();
            Files.copy(stream, copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return copy;
        } catch (IOException | SdkException e) {
            System.out.println("Could not read the object from S3: " + e.getMessage());
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // nothing more to do
            }
        }
    }

}
