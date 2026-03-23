package crm.view;

import crm.entity.User;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletRequest;
/**
 * CSV View Implementation for User Export
 * 
 * CLOUD READINESS: This view is now stateless and cloud-ready.
 * - Sessions are managed by Redis (distributed session store)
 * - No server affinity required - works with load balancers
 * - Horizontally scalable across multiple instances
 * - Data is passed through model (stateless pattern)
 * 
 * The view generates CSV files on-demand without storing state.
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
