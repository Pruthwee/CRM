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
 * PDF View implementation for exporting User data.
 * 
 * CLOUD-NATIVE STATELESS DESIGN:
 * This view is fully stateless and cloud-ready:
 * - Receives all data via the model (request-scoped)
 * - Does NOT access or store HTTP session state
 * - PDF document is created in-memory (no file system dependencies)
 * - Writes directly to response output stream via ByteArrayOutputStream
 * - Safe for horizontal scaling across multiple instances
 * 
 * Session Management:
 * If session data is needed, it is now managed by Spring Session with Redis,
 * allowing stateless application instances that can scale horizontally.
 * 
 * Cloud Deployment:
 * - Compatible with AWS ECS, EKS, Lambda, and other platforms
 * - Works correctly behind load balancers without sticky sessions
 * - No server affinity required
 * - Stateless operation enables auto-scaling
 * - Memory-efficient in-memory PDF generation
 */
public class PdfView extends AbstractPdfView {

    /**
     * Builds PDF document from user data in the model.
     * 
     * STATELESS IMPLEMENTATION:
     * - All data comes from the model parameter (request-scoped)
     * - No session state is accessed or modified
     * - PDF document is created in-memory
     * - No temporary files or server-side storage used
     * - Document is written directly to response stream
     * 
     * @param model Contains "users" list - passed from controller (request-scoped)
     * @param document In-memory PDF document (no file system dependencies)
     * @param writer PDF writer for in-memory generation
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for PDF output
     */
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // change the file name
        response.setHeader("Content-Disposition", "attachment; filename=\"my-pdf-file.pdf\"");

        // Get users from request-scoped model (NOT from session)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        document.add(new Paragraph("Generated Users " + LocalDate.now()));

        // Create PDF table - all in-memory, no file system access
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

        // Populate data rows - all processing is stateless and request-scoped
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
