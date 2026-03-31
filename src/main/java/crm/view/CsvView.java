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
 * CSV view implementation for exporting User data.
 * 
 * CLOUD-READY: This view is stateless and does not access HTTP session state.
 * All data is passed through the model parameter, ensuring horizontal scalability
 * and compatibility with cloud environments (AWS, Azure, GCP).
 * 
 * The view can be safely load-balanced across multiple instances without
 * session affinity requirements.
 */
public class CsvView extends AbstractCsvView {

    /**
     * Builds the CSV document from the model data.
     * 
     * STATELESS IMPLEMENTATION: This method retrieves all data from the model parameter.
     * No session state is accessed, ensuring cloud-native stateless behavior.
     * 
     * @param model Contains the "users" list - all data needed for CSV generation
     * @param request Not used for session access - only for potential request metadata
     * @param response Target for CSV output with appropriate headers
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        // Retrieve data from model (stateless) - NOT from session
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Validate that required data is present in model
        if (users == null) {
            throw new IllegalStateException("Required 'users' data not found in model. " +
                    "Ensure all data is passed via model for stateless operation.");
        }
        
        // Define CSV structure
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        // Write data rows - all from model (stateless)
        for (User user : users) {
            csvWriter.write(user, header);
        }
        csvWriter.close();

    }

}
