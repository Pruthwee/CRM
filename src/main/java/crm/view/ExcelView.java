package crm.view;

import crm.entity.User;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Excel View implementation for exporting user data.
 * 
 * Cloud-Native Design:
 * - Stateless: No session state storage or access
 * - Request-scoped: All data passed via model Map
 * - In-memory: Excel generated in-memory (no file system)
 * - Streaming: Workbook written directly to response
 * - Scalable: Compatible with horizontal scaling and load balancing
 * - Ephemeral: No local state, suitable for containerized environments
 * 
 * This implementation follows 12-factor app principles:
 * - Stateless processes
 * - No local file system dependency
 * - Suitable for cloud deployment (AWS, Azure, GCP)
 */
public class ExcelView extends AbstractXlsView {

    /**
     * Builds Excel document from model data and streams to response.
     * 
     * Cloud-Ready Implementation:
     * - Uses only request-scoped data from model Map
     * - Does NOT access HttpSession (stateless)
     * - Workbook generated in-memory (no file system)
     * - Written directly to response stream
     * - No instance variables used for state storage
     * - Thread-safe and suitable for concurrent requests
     * 
     * @param model Request-scoped data containing users list
     * @param workbook POI Workbook for Excel generation (in-memory)
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for streaming Excel output
     * @throws Exception if Excel generation fails
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-xls-file.xls\"");

        // Extract data from request-scoped model (stateless operation)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");

        // Create excel xls sheet (in-memory, no file system)
        Sheet sheet = workbook.createSheet("User Detail");
        sheet.setDefaultColumnWidth(30);

        // Create style for header cells
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setBold(true);
        font.setColor(HSSFColor.WHITE.index);
        style.setFont(font);

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("FirstName");
        header.getCell(0).setCellStyle(style);
        header.createCell(1).setCellValue("LastName");
        header.getCell(1).setCellStyle(style);
        header.createCell(2).setCellValue("Username");
        header.getCell(2).setCellStyle(style);
        header.createCell(3).setCellValue("Email");
        header.getCell(3).setCellStyle(style);
        header.createCell(4).setCellValue("Password");
        header.getCell(4).setCellStyle(style);
        header.createCell(5).setCellValue("Enabled");
        header.getCell(5).setCellStyle(style);
        header.createCell(6).setCellValue("Role_id");
        header.getCell(6).setCellStyle(style);
        header.createCell(7).setCellValue("Role_name");
        header.getCell(7).setCellStyle(style);

        int rowCount = 1;

        // Process each user record (stateless processing)
        for(User user : users){
            Row userRow =  sheet.createRow(rowCount++);
            userRow.createCell(0).setCellValue(user.getFirstName());
            userRow.createCell(1).setCellValue(user.getLastName());
            userRow.createCell(2).setCellValue(user.getUsername());
            userRow.createCell(3).setCellValue(user.getEmail());
            userRow.createCell(4).setCellValue(user.getPassword());
            userRow.createCell(5).setCellValue(user.getEnabled());
            userRow.createCell(6).setCellValue(user.getRole().getId());
            userRow.createCell(7).setCellValue(user.getRole().getName());
        }
        
        // Workbook is automatically written to response by AbstractXlsView
        // No file system operations, all in-memory
    }
}
