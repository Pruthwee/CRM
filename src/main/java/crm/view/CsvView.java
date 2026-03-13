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
 * Cloud-Ready CSV View for User Export
 * - Stateless implementation for horizontal scaling
 * - No session dependencies
 * - Compatible with cloud load balancers and API gateways
 * - Uses only model data for rendering
 */
public class CsvView extends AbstractCsvView {

    /**
     * Build CSV document from model data (stateless)
     * Does not rely on HTTP session state for cloud compatibility
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, 
                                   HttpServletResponse response) throws Exception {

        // Set download headers for cloud-compatible response
        response.setHeader("Content-Disposition", "attachment; filename=\"users-export.csv\"");

        // Extract data from model (stateless approach)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        if (users == null || users.isEmpty()) {
            // Handle empty data gracefully
            response.getWriter().write("No users available for export");
            return;
        }

        // Define CSV headers
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        // Create CSV writer with standard preferences
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        try {
            // Write header row
            csvWriter.writeHeader(header);

            // Write data rows (stateless iteration)
            for (User user : users) {
                csvWriter.write(user, header);
            }
        } finally {
            // Ensure resources are closed
            if (csvWriter != null) {
                csvWriter.close();
            }
        }
    }
}
