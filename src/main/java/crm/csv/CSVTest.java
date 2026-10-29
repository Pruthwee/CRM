package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;
import crm.utils.S3Storage;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVTest {

    public static void main(String[] args) {
//        System.out.println(document.getName());

        CSVReader reader;
        List<Object[]> data = new ArrayList<>();
        try {
            String key = System.getenv(ReadDataUtils.KEY_ENV);
            if (key == null || key.trim().isEmpty()) {
                throw new IOException(ReadDataUtils.KEY_ENV + " is not set");
            }
            reader = new CSVReader(new InputStreamReader(S3Storage.open(key)));
            String[] line;
            while ((line = reader.readNext()) != null) {
//                System.out.println(line[1] + "\t" + line[2]);
                data.add(line);
                if(line[1].equals("QUICK SUB")){
                    System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
                }

            }
        } catch (IOException | SdkException e) {
            e.printStackTrace();
        }
		/*System.out.println(data.get(0)[1] + "\t" + data.get(0)[2]);
		System.out.println(data.get(1)[1] + "\t" + data.get(1)[2]);*/
    }

}
