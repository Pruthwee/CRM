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
 * Cloud-Ready PDF View for User Export
 * - Stateless implementation for horizontal scaling
 * - No session dependencies
 * - Compatible with cloud load balancers and API gateways
 * - Uses only model data for rendering
 * - Memory-efficient PDF generation
 */
public class PdfView extends AbstractPdfView {

    /**
     * Build PDF document from model data (stateless)
     * Does not rely on HTTP session state for cloud compatibility
     */
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, 
                                   HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // Set download headers for cloud-compatible response
        response.setHeader("Content-Disposition", "attachment; filename=\"users-export.pdf\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Extract data from model (stateless approach)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        if (users == null || users.isEmpty()) {
            // Handle empty data gracefully
            document.add(new Paragraph("No users available for export"));
            return;
        }

        // Add document title with generation date
        document.add(new Paragraph("Generated Users Report - " + LocalDate.now()));
        document.add(new Paragraph(" ")); // Add spacing

        // Create table with appropriate column count
        PdfPTable table = new PdfPTable(8); // 8 columns for user data
        table.setWidthPercentage(100.0f);
        table.setSpacingBefore(10);

        // Define font for table header row
        Font font = FontFactory.getFont(FontFactory.TIMES);
        font.setColor(BaseColor.WHITE);

        // Define table header cell style
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(5);

        // Write table headers
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

        cell.setPhrase(new Phrase("Role ID", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Role Name", font));
        table.addCell(cell);

        // Write data rows (stateless iteration)
        for (User user : users) {
            table.addCell(user.getFirstName() != null ? user.getFirstName() : "");
            table.addCell(user.getLastName() != null ? user.getLastName() : "");
            table.addCell(user.getUsername() != null ? user.getUsername() : "");
            table.addCell(user.getEmail() != null ? user.getEmail() : "");
            table.addCell(user.getPassword() != null ? user.getPassword() : "");
            table.addCell(String.valueOf(user.getEnabled()));
            
            // Safely handle role information
            if (user.getRole() != null) {
                table.addCell(String.valueOf(user.getRole().getId()));
                table.addCell(user.getRole().getName() != null ? user.getRole().getName() : "");
            } else {
                table.addCell("");
                table.addCell("");
            }
        }

        // Add table to document
        document.add(table);
    }
}
