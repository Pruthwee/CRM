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
 * CSV View implementation for exporting User data.
 * 
 * CLOUD-NATIVE STATELESS DESIGN:
 * This view is fully stateless and cloud-ready:
 * - Receives all data via the model (request-scoped)
 * - Does NOT access or store HTTP session state
 * - Does NOT use file system for temporary storage
 * - Writes directly to response output stream
 * - Safe for horizontal scaling across multiple instances
 * 
 * Cloud Deployment:
 * - Compatible with AWS ECS, EKS, Lambda, and other platforms
 * - Works correctly behind load balancers without sticky sessions
 * - No server affinity required
 * - Stateless operation enables auto-scaling
 */
public class CsvView extends AbstractCsvView {

    /**
     * Builds CSV document from user data in the model.
     * 
     * STATELESS IMPLEMENTATION:
     * - All data comes from the model parameter (request-scoped)
     * - No session state is accessed or modified
     * - CSV is written directly to response stream
     * - No temporary files or server-side storage used
     * 
     * @param model Contains "users" list - passed from controller (request-scoped)
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for CSV output
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        // Get users from request-scoped model (NOT from session)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        // Write directly to response stream - no file system dependencies
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        for (User user : users) {
            csvWriter.write(user, header);
        }
        csvWriter.close();

    }

}
