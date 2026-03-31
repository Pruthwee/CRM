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
 * Excel View for User Export - Cloud-Native Implementation
 * 
 * Cloud Readiness Features:
 * - Stateless operation: No session state dependencies
 * - Request-scoped data: All user data from model parameter
 * - No server affinity: Can execute on any instance
 * - Horizontal scaling ready: No instance-specific state
 * - Distributed session compatible: Works with Redis session store
 * 
 * This view generates Excel files directly from request-scoped model data,
 * making it fully compatible with cloud load balancers and distributed
 * session management systems.
 */
public class ExcelView extends AbstractXlsView{

    /**
     * Build Excel document from model data (stateless operation)
     * 
     * Cloud-Native Pattern:
     * - Retrieves user list from model (request-scoped)
     * - No HTTP session access or modification
     * - Workbook created in memory (stateless)
     * - Writes directly to response output stream
     * - No server-side state persistence
     * 
     * This ensures the view works correctly in a horizontally scaled
     * cloud environment with multiple application instances and
     * distributed session management.
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-xls-file.xls\"");

        // Retrieve user data from request-scoped model (not from session)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");

        // Create excel xls sheet - all operations are stateless
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

        // Write user data - all from request-scoped model
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
        // No session state is stored or accessed
    }

}
