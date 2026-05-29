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

public class CSVTest {

    private static final Logger logger = LoggerFactory.getLogger(CSVTest.class);

    public static void main(String[] args) {
        File document = ReadDataUtils.ReadFile("Select CSV file", null, "Only CSV Files", "csv");
//        logger.debug("File name: {}", document.getName());

        CSVReader reader;
        List<Object[]> data = new ArrayList<>();
        try {
            reader = new CSVReader(new FileReader(document));
            String[] line;
            while ((line = reader.readNext()) != null) {
//                logger.debug("Line data: {} \t {}", line[1], line[2]);
                data.add(line);
                if(line[1].equals("QUICK SUB")){
                    logger.info("Found QUICK SUB: {} \t {} \t {}", line[0], line[1], line[2]);
                }

            }
        } catch (IOException e) {
            logger.error("Error reading CSV file", e);
        }
		/*logger.debug("First row: {} \t {}", data.get(0)[1], data.get(0)[2]);
		logger.debug("Second row: {} \t {}", data.get(1)[1], data.get(1)[2]);*/
    }

}
