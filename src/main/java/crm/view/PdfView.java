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
 * PDF view implementation for exporting User data.
 * 
 * CLOUD-READY: This view is stateless and does not access HTTP session state.
 * All data is passed through the model parameter, ensuring horizontal scalability
 * and compatibility with cloud environments (AWS, Azure, GCP).
 * 
 * The view can be safely load-balanced across multiple instances without
 * session affinity requirements.
 */
public class PdfView extends AbstractPdfView {

    /**
     * Builds the PDF document from the model data.
     * 
     * STATELESS IMPLEMENTATION: This method retrieves all data from the model parameter.
     * No session state is accessed, ensuring cloud-native stateless behavior.
     * 
     * @param model Contains the "users" list - all data needed for PDF generation
     * @param document The PDF document to populate
     * @param writer The PDF writer
     * @param request Not used for session access - only for potential request metadata
     * @param response Target for PDF output with appropriate headers
     */
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-pdf-file.pdf\"");

        // Retrieve data from model (stateless) - NOT from session
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Validate that required data is present in model
        if (users == null || users.isEmpty()) {
            throw new IllegalStateException("Required 'users' data not found in model or is empty. " +
                    "Ensure all data is passed via model for stateless operation.");
        }
        
        // Add document title with generation date
        document.add(new Paragraph("Generated Users " + LocalDate.now()));

        // Create table structure
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

        // Write data rows - all from model (stateless)
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
