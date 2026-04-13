package crm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class ReadDataUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReadDataUtils.class);

    public static File ReadFile(String dialogMEssage, JFrame parent, String fileExtensionDescription,
                                String... fileExtension) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(fileExtensionDescription, fileExtension);
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            logger.info("You chose to open this file: {}", chooser.getSelectedFile().getName());
            return chooser.getSelectedFile();
        }
        return null;
    }

}
