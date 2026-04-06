package crm.view;

import crm.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Cloud-ready CSV view implementation for user data export.
 * Compatible with embedded servlet containers and cloud platforms.
 */
public class CsvView extends AbstractCsvView {

    private static final Logger log = LoggerFactory.getLogger(CsvView.class);

    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("Content-Disposition", "attachment; filename=\"my-csv-file.csv\"");

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        if (users == null || users.isEmpty()) {
            log.warn("No users found in model for CSV export");
            return;
        }
        
        String[] header = {"FirstName", "LastName", "Username", "Email", "Password", "Enabled", "Role_id", "Role_name"};
        
        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE)) {
            csvWriter.writeHeader(header);

            for (User user : users) {
                csvWriter.write(user, header);
            }
            
            log.debug("Successfully exported {} users to CSV", users.size());
        } catch (Exception e) {
            log.error("Error writing CSV data: {}", e.getMessage(), e);
            throw e;
        }
    }

}
