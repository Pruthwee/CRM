package crm.view;

import crm.entity.User;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Cloud-ready Excel view implementation for user data export.
 * Compatible with embedded servlet containers and cloud platforms.
 * Generates Excel files in memory without file system dependencies.
 */
public class ExcelView extends AbstractXlsView {

    private static final Logger log = LoggerFactory.getLogger(ExcelView.class);

    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // Set file name for download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-xls-file.xls\"");

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        if (users == null || users.isEmpty()) {
            log.warn("No users found in model for Excel export");
            return;
        }

        try {
            // Create Excel sheet
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

            // Populate data rows
            int rowCount = 1;
            for(User user : users){
                Row userRow = sheet.createRow(rowCount++);
                userRow.createCell(0).setCellValue(user.getFirstName());
                userRow.createCell(1).setCellValue(user.getLastName());
                userRow.createCell(2).setCellValue(user.getUsername());
                userRow.createCell(3).setCellValue(user.getEmail());
                userRow.createCell(4).setCellValue(user.getPassword());
                userRow.createCell(5).setCellValue(user.getEnabled());
                userRow.createCell(6).setCellValue(user.getRole().getId());
                userRow.createCell(7).setCellValue(user.getRole().getName());
            }
            
            log.debug("Successfully exported {} users to Excel", users.size());
        } catch (Exception e) {
            log.error("Error building Excel document: {}", e.getMessage(), e);
            throw e;
        }
    }

}
