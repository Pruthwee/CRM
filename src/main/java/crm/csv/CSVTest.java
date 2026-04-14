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
//        System.out.println(document.getName());

        CSVReader reader;
        List<Object[]> data = new ArrayList<>();
        try {
            reader = new CSVReader(new FileReader(document));
            String[] line;
            while ((line = reader.readNext()) != null) {
//                System.out.println(line[1] + "\t" + line[2]);
                data.add(line);
                if(line[1].equals("QUICK SUB")){
                    logger.info("{}\t{}\t{}", line[0], line[1], line[2]);
                }

            }
        } catch (IOException e) {
            logger.error("Error reading CSV file", e);
        }
		/*System.out.println(data.get(0)[1] + "\t" + data.get(0)[2]);
		System.out.println(data.get(1)[1] + "\t" + data.get(1)[2]);*/
    }

}
