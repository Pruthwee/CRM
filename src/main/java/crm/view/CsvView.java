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
 * CSV View for User Export - Cloud-Native Implementation
 * 
 * Cloud Readiness Features:
 * - Stateless operation: No session state dependencies
 * - Request-scoped data: All user data from model parameter
 * - No server affinity: Can execute on any instance
 * - Horizontal scaling ready: No instance-specific state
 * - Distributed session compatible: Works with Redis session store
 * 
 * This view generates CSV files directly from request-scoped model data,
 * making it fully compatible with cloud load balancers and distributed
 * session management systems.
 */
public class CsvView extends AbstractCsvView {

    /**
     * Build CSV document from model data (stateless operation)
     * 
     * Cloud-Native Pattern:
     * - Retrieves user list from model (request-scoped)
     * - No HTTP session access or modification
     * - Writes directly to response output stream
     * - No server-side state persistence
     * 
     * This ensures the view works correctly in a horizontally scaled
     * cloud environment with multiple application instances.
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        // Retrieve user data from request-scoped model (not from session)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Define CSV structure
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        // Create CSV writer - writes directly to response (stateless)
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        // Write CSV header
        csvWriter.writeHeader(header);

        // Write user data - all from request-scoped model
        for (User user : users) {
            csvWriter.write(user, header);
        }
        
        // Close writer and flush to response
        csvWriter.close();
    }

}
