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
 * CSV View - Cloud-Native Stateless Implementation
 * 
 * This view has been refactored to be stateless and cloud-ready:
 * - No session state storage or retrieval
 * - All data passed through model (request-scoped)
 * - Compatible with horizontal scaling
 * - Works with distributed session stores (Redis/Hazelcast)
 * - No instance variables that hold state
 */
public class CsvView extends AbstractCsvView {

    /**
     * Builds CSV document using only request-scoped data from the model.
     * This implementation is completely stateless:
     * - Does not access HTTP session
     * - Does not store any state in instance variables
     * - All data comes from the model parameter
     * - Output is written directly to response stream
     * 
     * This ensures the view works correctly in cloud environments with:
     * - Multiple application instances
     * - Load balancing across instances
     * - Distributed session management
     * 
     * @param model Request-scoped model data containing users list
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for writing CSV output
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Set response headers for file download (stateless operation)
        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        // Extract data from request-scoped model (no session access)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Define CSV structure
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        // Create CSV writer using response output stream (stateless, no file system)
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        // Write CSV header
        csvWriter.writeHeader(header);

        // Write user data from model (stateless iteration)
        for (User user : users) {
            csvWriter.write(user, header);
        }
        
        // Close writer to flush output
        csvWriter.close();
    }

}
