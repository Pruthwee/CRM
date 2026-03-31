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
 * CLOUD READINESS IMPLEMENTATION (AWS Compatible):
 * ================================================
 * This view is fully stateless and cloud-ready for horizontal scaling:
 * 
 * 1. NO HTTP SESSION DEPENDENCIES:
 *    - Does NOT access HttpSession.getAttribute() or setAttribute()
 *    - All user data is retrieved from the model parameter (passed by controller)
 *    - No session state is accessed or stored during CSV generation
 * 
 * 2. STATELESS DESIGN PRINCIPLES:
 *    - No instance variables store request-specific data
 *    - All data flows through method parameters (model, request, response)
 *    - Each request is independent and can be handled by any instance
 *    - No server affinity required for load balancing
 * 
 * 3. CLOUD-NATIVE PATTERNS:
 *    - Compatible with AWS ECS, EKS, Elastic Beanstalk
 *    - Supports auto-scaling without data loss
 *    - Works with Application Load Balancer (no sticky sessions needed)
 *    - CSV is written directly to response stream (no file system dependency)
 *    - No temporary files created on disk
 * 
 * 4. IN-MEMORY PROCESSING:
 *    - CSV is written directly to response.getWriter()
 *    - No file system dependency (cloud-friendly)
 *    - Compatible with ephemeral container storage
 *    - Works in read-only file systems
 * 
 * USAGE PATTERN:
 * ==============
 * Controllers should pass all required data through the model:
 * 
 *   @GetMapping("/export/users/csv")
 *   public String exportUsersCsv(Model model) {
 *       List<User> users = userService.listAllUsers();
 *       model.addAttribute("users", users);
 *       return "csvView";
 *   }
 * 
 * DO NOT use session.setAttribute() for view data.
 * 
 * DISTRIBUTED SESSION MANAGEMENT:
 * ===============================
 * If session data is needed for authentication/authorization:
 * - Session is managed by Spring Session + Redis (AWS ElastiCache)
 * - Session data persists across instance restarts
 * - Multiple instances share session state transparently
 * - But view data should ALWAYS come from model, not session
 */
public class CsvView extends AbstractCsvView {

    /**
     * Builds CSV document from model data in a stateless manner.
     * 
     * CLOUD-NATIVE IMPLEMENTATION:
     * ============================
     * - All user data is retrieved from the model parameter (not from session)
     * - CSV is written directly to the HTTP response stream
     * - No file system dependency (no temporary files)
     * - No server-side state is created or accessed
     * - Compatible with distributed cloud environments
     * - Works in read-only file systems (container best practice)
     * 
     * PROCESS FLOW:
     * 1. Retrieve user list from model (passed by controller)
     * 2. Set response headers for file download
     * 3. Write CSV directly to response.getWriter()
     * 4. No temporary files or session state involved
     * 
     * @param model Contains "users" attribute with List<User> data
     * @param request HTTP request (not used for session access)
     * @param response HTTP response (CSV is written directly to output stream)
     */
    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        // Retrieve data from model (not from session) - cloud-native stateless pattern
        // This ensures the view works correctly in distributed cloud environments
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Validate that data is present (fail fast if controller didn't provide data)
        if (users == null) {
            throw new IllegalStateException("No 'users' data found in model. " +
                "Controller must add users to model: model.addAttribute(\"users\", userList)");
        }
        
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        // Write directly to response stream (no file system dependency)
        // This is cloud-friendly and works in ephemeral container storage
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        for (User user : users) {
            csvWriter.write(user, header);
        }
        csvWriter.close();

    }

}
