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
 * Cloud-Ready Excel View for User Export
 * - Stateless implementation for horizontal scaling
 * - No session dependencies
 * - Compatible with cloud load balancers and API gateways
 * - Uses only model data for rendering
 * - Memory-efficient workbook generation
 */
public class ExcelView extends AbstractXlsView {

    /**
     * Build Excel document from model data (stateless)
     * Does not rely on HTTP session state for cloud compatibility
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // Set download headers for cloud-compatible response
        response.setHeader("Content-Disposition", "attachment; filename=\"users-export.xls\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Extract data from model (stateless approach)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        if (users == null || users.isEmpty()) {
            // Handle empty data gracefully
            Sheet sheet = workbook.createSheet("User Detail");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("No users available for export");
            return;
        }

        // Create excel xls sheet
        Sheet sheet = workbook.createSheet("User Detail");
        sheet.setDefaultColumnWidth(30);

        // Create style for header cells
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setBold(true);
        font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
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

        // Write data rows (stateless iteration)
        int rowCount = 1;
        for (User user : users) {
            Row userRow = sheet.createRow(rowCount++);
            userRow.createCell(0).setCellValue(user.getFirstName());
            userRow.createCell(1).setCellValue(user.getLastName());
            userRow.createCell(2).setCellValue(user.getUsername());
            userRow.createCell(3).setCellValue(user.getEmail());
            userRow.createCell(4).setCellValue(user.getPassword());
            userRow.createCell(5).setCellValue(user.getEnabled());
            
            // Safely handle role information
            if (user.getRole() != null) {
                userRow.createCell(6).setCellValue(user.getRole().getId());
                userRow.createCell(7).setCellValue(user.getRole().getName());
            } else {
                userRow.createCell(6).setCellValue("");
                userRow.createCell(7).setCellValue("");
            }
        }
    }
}
