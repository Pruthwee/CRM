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
 * PDF View for User Export - Cloud-Native Implementation
 * 
 * Cloud Readiness Features:
 * - Stateless operation: No session state dependencies
 * - Request-scoped data: All user data from model parameter
 * - No server affinity: Can execute on any instance
 * - Horizontal scaling ready: No instance-specific state
 * - Distributed session compatible: Works with Redis session store
 * 
 * This view generates PDF files directly from request-scoped model data,
 * making it fully compatible with cloud load balancers and distributed
 * session management systems.
 */
public class PdfView extends AbstractPdfView {

    /**
     * Build PDF document from model data (stateless operation)
     * 
     * Cloud-Native Pattern:
     * - Retrieves user list from model (request-scoped)
     * - No HTTP session access or modification
     * - PDF document created in memory (stateless)
     * - Writes directly to response output stream
     * - No server-side state persistence
     * 
     * This ensures the view works correctly in a horizontally scaled
     * cloud environment with multiple application instances and
     * distributed session management.
     */
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-pdf-file.pdf\"");

        // Retrieve user data from request-scoped model (not from session)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Add document title with current date
        document.add(new Paragraph("Generated Users " + LocalDate.now()));

        // Create PDF table - all operations are stateless
        PdfPTable table = new PdfPTable(users.stream().findAny().get().getColumnCount());
        table.setWidthPercentage(100.0f);
        table.setSpacingBefore(10);

        // Define font for table header row
        Font font = FontFactory.getFont(FontFactory.TIMES);
        font.setColor(BaseColor.WHITE);

        // Define table header cell
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(5);

        // Write table header
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

        // Write user data - all from request-scoped model
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

        // Add table to document
        document.add(table);
        
        // Document is automatically written to response by AbstractPdfView
        // No session state is stored or accessed
    }

}
