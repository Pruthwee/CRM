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
 * Excel View implementation for exporting User data.
 * 
 * CLOUD-NATIVE STATELESS DESIGN:
 * This view is fully stateless and cloud-ready:
 * - Receives all data via the model (request-scoped)
 * - Does NOT access or store HTTP session state
 * - Excel workbook is created in-memory (no file system dependencies)
 * - Writes directly to response output stream
 * - Safe for horizontal scaling across multiple instances
 * 
 * Session Management:
 * If session data is needed, it is now managed by Spring Session with Redis,
 * allowing stateless application instances that can scale horizontally.
 * 
 * Cloud Deployment:
 * - Compatible with AWS ECS, EKS, Lambda, and other platforms
 * - Works correctly behind load balancers without sticky sessions
 * - No server affinity required
 * - Stateless operation enables auto-scaling
 * - Memory-efficient in-memory Excel generation
 */
public class ExcelView extends AbstractXlsView{

    /**
     * Builds Excel document from user data in the model.
     * 
     * STATELESS IMPLEMENTATION:
     * - All data comes from the model parameter (request-scoped)
     * - No session state is accessed or modified
     * - Excel workbook is created in-memory
     * - No temporary files or server-side storage used
     * - Workbook is written directly to response stream
     * 
     * @param model Contains "users" list - passed from controller (request-scoped)
     * @param workbook In-memory Excel workbook (no file system dependencies)
     * @param request HTTP request (not used for session access)
     * @param response HTTP response for Excel output
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // change the file name
        response.setHeader("Content-Disposition", "attachment; filename=\"my-xls-file.xls\"");

        // Get users from request-scoped model (NOT from session)
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");

        // create excel xls sheet - all in-memory, no file system access
        Sheet sheet = workbook.createSheet("User Detail");
        sheet.setDefaultColumnWidth(30);

        // create style for header cells
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setBold(true);
        font.setColor(HSSFColor.WHITE.index);
        style.setFont(font);


        // create header row
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

        // Populate data rows - all processing is stateless and request-scoped
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

    }

}
