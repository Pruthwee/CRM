package crm.view;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Cloud-ready PDF view implementation for user data export.
 * Compatible with embedded servlet containers and cloud platforms.
 * Generates PDFs in memory without file system dependencies.
 */
public class PdfView extends AbstractPdfView {

    private static final Logger log = LoggerFactory.getLogger(PdfView.class);

    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, 
                                   HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // Set file name for download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-pdf-file.pdf\"");

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        if (users == null || users.isEmpty()) {
            log.warn("No users found in model for PDF export");
            document.add(new Paragraph("No users available"));
            return;
        }

        try {
            // Use UTC for consistent timestamps across cloud regions
            LocalDate currentDate = LocalDate.now(ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            document.add(new Paragraph("Generated Users " + currentDate.format(formatter)));

            // Create table
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

            // Populate table rows
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
            
            log.debug("Successfully exported {} users to PDF", users.size());
        } catch (Exception e) {
            log.error("Error building PDF document: {}", e.getMessage(), e);
            throw e;
        }
    }

}
