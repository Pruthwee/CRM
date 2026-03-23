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
 * PDF View - Cloud-Native Stateless Implementation
 * 
 * This view has been refactored to be stateless and cloud-ready:
 * - No session state storage or retrieval
 * - All data passed through model (request-scoped)
 * - Compatible with horizontal scaling
 * - Works with distributed session stores (Redis/Hazelcast)
 * - No instance variables that hold state
 * - Uses in-memory document generation (no file system dependencies)
 */
public class PdfView extends AbstractPdfView {

    /**
     * Builds PDF document using only request-scoped data from the model.
     * This implementation is completely stateless:
     * - Does not access HTTP session
     * - Does not store any state in instance variables
     * - All data comes from the model parameter
     * - Document is created in-memory (no file system dependency)
     * - Output is written directly to response stream
     * 
     * This ensures the view works correctly in cloud environments with:
     * - Multiple application instances
     * - Load balancing across instances
     * - Distributed session management
     * - No local file system dependencies
     * 
     * @param model Request-scoped model data containing users list
     * @param document In-memory PDF document (stateless)
     * @param writer PDF writer for output
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for writing PDF output
     */
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // Set response headers for file download (stateless operation)
        response.setHeader("Content-Disposition", "attachment; filename=\"my-pdf-file.pdf\"");

        // Extract data from request-scoped model (no session access)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Add document title with current date (stateless operation)
        document.add(new Paragraph("Generated Users " + LocalDate.now()));

        // Create PDF table in-memory (stateless, no file system)
        PdfPTable table = new PdfPTable(users.stream().findAny().get().getColumnCount());
        table.setWidthPercentage(100.0f);
        table.setSpacingBefore(10);

        // Define font for table header row (stateless operation)
        Font font = FontFactory.getFont(FontFactory.TIMES);
        font.setColor(BaseColor.WHITE);

        // Define table header cell style (stateless operation)
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(5);

        // Write table header (stateless operation)
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

        // Write user data from model (stateless iteration)
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

        // Add table to document (stateless operation)
        document.add(table);
    }

}
