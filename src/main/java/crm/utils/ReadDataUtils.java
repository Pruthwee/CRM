package crm.utils;

import java.io.File;

/**
 * Legacy utility class with GUI file chooser.
 * This class is deprecated for containerized deployments.
 * Use REST API file upload endpoints instead.
 */
@Deprecated
public class ReadDataUtils {

    /**
     * @deprecated This method uses Swing GUI components which are incompatible with headless container environments.
     * Replace with REST API file upload functionality.
     */
    @Deprecated
    public static File ReadFile(String dialogMEssage, Object parent, String fileExtensionDescription,
                                String... fileExtension) {
        throw new UnsupportedOperationException(
            "GUI-based file selection is not supported in containerized environments. " +
            "Please use REST API file upload endpoints instead."
        );
    }

}
