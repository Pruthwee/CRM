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
 * CSV View implementation for exporting user data in a cloud-native stateless manner.
 * 
 * CLOUD READINESS IMPLEMENTATION:
 * - All data is retrieved from the model parameter (passed by controller)
 * - No session state is accessed or stored
 * - CSV is written directly to response output stream (no file system dependency)
 * - Stateless design allows horizontal scaling across multiple instances
 * - Compatible with AWS, Azure, and GCP cloud environments
 * 
 * USAGE:
 * Controllers should pass all required data through the model:
 * model.addAttribute("users", userList);
 * 
 * Do NOT store data in session attributes for view rendering.
 */
public class CsvView extends AbstractCsvView {

    /**
     * Builds CSV document from model data in a stateless manner.
     * All user data is retrieved from the model parameter, not from session.
     * CSV is written directly to the HTTP response stream.
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        // Retrieve data from model (not from session) - cloud-native stateless pattern
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        // Write directly to response stream (no file system dependency)
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        for (User user : users) {
            csvWriter.write(user, header);
        }
        csvWriter.close();

    }

}
