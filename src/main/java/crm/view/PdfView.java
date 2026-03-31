package crm.view;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * PDF View implementation for exporting user data in a cloud-native stateless manner.
 * 
 * CLOUD READINESS IMPLEMENTATION (AWS Compatible):
 * ================================================
 * This view is fully stateless and cloud-ready for horizontal scaling:
 * 
 * 1. NO HTTP SESSION DEPENDENCIES:
 *    - Does NOT access HttpSession.getAttribute() or setAttribute()
 *    - All user data is retrieved from the model parameter (passed by controller)
 *    - No session state is accessed or stored during PDF generation
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
 *    - PDF document is generated in-memory and written directly to response stream
 *    - No file system dependency (uses ByteArrayOutputStream internally)
 *    - No temporary files created on disk
 * 
 * 4. IN-MEMORY PROCESSING:
 *    - PDF is generated in-memory using ByteArrayOutputStream
 *    - Written directly to response output stream
 *    - No file system dependency (cloud-friendly)
 *    - Compatible with ephemeral container storage
 *    - Works in read-only file systems
 * 
 * USAGE PATTERN:
 * ==============
 * Controllers should pass all required data through the model:
 * 
 *   @GetMapping("/export/users/pdf")
 *   public String exportUsersPdf(Model model) {
 *       List<User> users = userService.listAllUsers();
 *       model.addAttribute("users", users);
 *       return "pdfView";
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
public class PdfView extends AbstractPdfView {

    /**
     * Builds PDF document from model data in a stateless manner.
     * 
     * CLOUD-NATIVE IMPLEMENTATION:
     * ============================
     * - All user data is retrieved from the model parameter (not from session)
     * - PDF is generated in-memory using ByteArrayOutputStream
     * - PDF is written directly to the HTTP response stream
     * - No file system dependency (no temporary files)
     * - No server-side state is created or accessed
     * - Compatible with distributed cloud environments
     * - Works in read-only file systems (container best practice)
     * 
     * PROCESS FLOW:
     * 1. Retrieve user list from model (passed by controller)
     * 2. Set response headers for file download
     * 3. Create PDF document in-memory
     * 4. Populate PDF with user data
     * 5. Write PDF directly to response stream
     * 6. No temporary files or session state involved
     * 
     * @param model Contains "users" attribute with List<User> data
     * @param document iText PDF document to populate
     * @param writer PDF writer for document generation
     * @param request HTTP request (not used for session access)
     * @param response HTTP response (PDF is written directly to output stream)
     */
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // change the file name
        response.setHeader("Content-Disposition", "attachment; filename=\"my-pdf-file.pdf\"");

        // Retrieve data from model (not from session) - cloud-native stateless pattern
        // This ensures the view works correctly in distributed cloud environments
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Validate that data is present (fail fast if controller didn't provide data)
        if (users == null || users.isEmpty()) {
            throw new IllegalStateException("No 'users' data found in model. " +
                "Controller must add users to model: model.addAttribute(\"users\", userList)");
        }
        
        document.add(new Paragraph("Generated Users " + LocalDate.now()));

        PdfPTable table = new PdfPTable(users.stream().findAny().get().getColumnCount());
        table.setWidthPercentage(100.0f);
        table.setSpacingBefore(10);

        // define font for table header row
        Font font = FontFactory.getFont(FontFactory.TIMES);
        font.setColor(BaseColor.WHITE);

        // define table header cell
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(5);

        // write table header
        cell.setPhrase(new Phrase("First Name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Last Name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Username", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Email", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Password", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Enabled", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Role_id", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Role_name", font));
        table.addCell(cell);

        // Populate table with user data from model (not from session)
        // All data is passed through the model parameter for cloud compatibility
        for(User user : users){
            table.addCell(user.getFirstName());
            table.addCell(user.getLastName());
            table.addCell(user.getUsername());
            table.addCell(user.getEmail());
            table.addCell(user.getPassword());
            table.addCell(String.valueOf(user.getEnabled()));
            table.addCell(String.valueOf(user.getRole().getId()));
            table.addCell(user.getRole().getName());
        }

        document.add(table);
    }

}
