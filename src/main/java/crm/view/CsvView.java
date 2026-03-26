package crm.view;

import crm.entity.User;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * CSV view implementation for exporting user data.
 * 
 * CLOUD-NATIVE PATTERN:
 * This view class is cloud-ready and works with distributed session management.
 * Session data is stored in Redis (configured via RedisSessionConfig),
 * enabling stateless horizontal scaling in cloud environments.
 * 
 * The implementation:
 * - Retrieves all data from the model (stateless)
 * - Does not store any state in instance variables
 * - Uses request/response only for reading parameters and writing output
 * - Session data (if needed) is automatically managed by Spring Session Redis
 * 
 * This makes the view compatible with cloud-native stateless architectures
 * and allows the application to scale horizontally across multiple instances.
 */
public class CsvView extends AbstractCsvView {

    /**
     * Builds the CSV document from the model data.
     * 
     * CLOUD-READY: This method is stateless and retrieves all data from the model.
     * No session state is stored in instance variables, making it compatible
     * with cloud environments where instances can be terminated or scaled.
     * 
     * @param model the model containing user data
     * @param request the HTTP request (session managed by Redis)
     * @param response the HTTP response
     * @throws Exception if CSV generation fails
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        for (User user : users) {
            csvWriter.write(user, header);
        }
        csvWriter.close();

    }

}
