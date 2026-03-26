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
 * CSV View implementation for exporting user data.
 * 
 * Cloud-Native Design:
 * - Stateless: No session state storage or access
 * - Request-scoped: All data passed via model Map
 * - Streaming: CSV written directly to response (no file system)
 * - Scalable: Compatible with horizontal scaling and load balancing
 * - Ephemeral: No local state, suitable for containerized environments
 * 
 * This implementation follows 12-factor app principles:
 * - Stateless processes
 * - No local file system dependency
 * - Suitable for cloud deployment (AWS, Azure, GCP)
 */
public class CsvView extends AbstractCsvView {

    /**
     * Builds CSV document from model data and streams to response.
     * 
     * Cloud-Ready Implementation:
     * - Uses only request-scoped data from model Map
     * - Does NOT access HttpSession (stateless)
     * - Writes directly to response stream (no file system)
     * - No instance variables used for state storage
     * - Thread-safe and suitable for concurrent requests
     * 
     * @param model Request-scoped data containing users list
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for streaming CSV output
     * @throws Exception if CSV generation fails
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        // Extract data from request-scoped model (stateless operation)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Define CSV structure
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        // Write CSV directly to response stream (no file system, no session state)
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        // Stream each user record (stateless processing)
        for (User user : users) {
            csvWriter.write(user, header);
        }
        
        csvWriter.close();
    }
}
