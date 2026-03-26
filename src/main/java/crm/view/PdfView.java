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
 * PDF View implementation for exporting user data.
 * 
 * Cloud-Native Design:
 * - Stateless: No session state storage or access
 * - Request-scoped: All data passed via model Map
 * - In-memory: PDF generated in-memory (no file system)
 * - Streaming: PDF written directly to response
 * - Scalable: Compatible with horizontal scaling and load balancing
 * - Ephemeral: No local state, suitable for containerized environments
 * 
 * This implementation follows 12-factor app principles:
 * - Stateless processes
 * - No local file system dependency
 * - Suitable for cloud deployment (AWS, Azure, GCP)
 */
public class PdfView extends AbstractPdfView {

    /**
     * Builds PDF document from model data and streams to response.
     * 
     * Cloud-Ready Implementation:
     * - Uses only request-scoped data from model Map
     * - Does NOT access HttpSession (stateless)
     * - PDF generated in-memory using ByteArrayOutputStream
     * - Written directly to response stream (no file system)
     * - No instance variables used for state storage
     * - Thread-safe and suitable for concurrent requests
     * 
     * @param model Request-scoped data containing users list
     * @param document iText Document for PDF generation (in-memory)
     * @param writer PdfWriter for rendering
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for streaming PDF output
     * @throws Exception if PDF generation fails
     */
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-pdf-file.pdf\"");

        // Extract data from request-scoped model (stateless operation)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Add document title with current date
        document.add(new Paragraph("Generated Users " + LocalDate.now()));

        // Create PDF table (in-memory, no file system)
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

        // Process each user record (stateless processing)
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

        // Add table to document (in-memory operation)
        document.add(table);
        
        // Document is automatically written to response by AbstractPdfView
        // No file system operations, all in-memory
    }
}
