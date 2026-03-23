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
 * Excel View - Cloud-Native Stateless Implementation
 * 
 * This view has been refactored to be stateless and cloud-ready:
 * - No session state storage or retrieval
 * - All data passed through model (request-scoped)
 * - Compatible with horizontal scaling
 * - Works with distributed session stores (Redis/Hazelcast)
 * - No instance variables that hold state
 * - Uses in-memory workbook (no file system dependencies)
 */
public class ExcelView extends AbstractXlsView{

    /**
     * Builds Excel document using only request-scoped data from the model.
     * This implementation is completely stateless:
     * - Does not access HTTP session
     * - Does not store any state in instance variables
     * - All data comes from the model parameter
     * - Workbook is created in-memory (no file system dependency)
     * - Output is written directly to response stream
     * 
     * This ensures the view works correctly in cloud environments with:
     * - Multiple application instances
     * - Load balancing across instances
     * - Distributed session management
     * - No local file system dependencies
     * 
     * @param model Request-scoped model data containing users list
     * @param workbook In-memory Excel workbook (stateless)
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for writing Excel output
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // Set response headers for file download (stateless operation)
        response.setHeader("Content-Disposition", "attachment; filename=\"my-xls-file.xls\"");

        // Extract data from request-scoped model (no session access)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");

        // Create excel xls sheet in-memory (stateless, no file system)
        Sheet sheet = workbook.createSheet("User Detail");
        sheet.setDefaultColumnWidth(30);

        // Create style for header cells (stateless operation)
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setBold(true);
        font.setColor(HSSFColor.WHITE.index);
        style.setFont(font);

        // Create header row (stateless operation)
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

        // Write user data from model (stateless iteration)
        int rowCount = 1;
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
    }

}
