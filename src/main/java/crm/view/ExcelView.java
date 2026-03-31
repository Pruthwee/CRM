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
 * Excel View implementation for exporting user data in a cloud-native stateless manner.
 * 
 * CLOUD READINESS IMPLEMENTATION (AWS Compatible):
 * ================================================
 * This view is fully stateless and cloud-ready for horizontal scaling:
 * 
 * 1. NO HTTP SESSION DEPENDENCIES:
 *    - Does NOT access HttpSession.getAttribute() or setAttribute()
 *    - All user data is retrieved from the model parameter (passed by controller)
 *    - No session state is accessed or stored during Excel generation
 * 
 * 2. STATELESS DESIGN PRINCIPLES:
 *    - No instance variables store request-specific data
 *    - All data flows through method parameters (model, request, response)
 *    - Each request is independent and can be handled by any instance
 *    - No server affinity required for load balancing
 * 
 * 3. CLOUD-NATIVE PATTERNS:
 *    - Compatible with AWS ECS, EKS, Elastic Beanstalk
 *    - Supports auto-scaling without data loss
 *    - Works with Application Load Balancer (no sticky sessions needed)
 *    - Excel workbook is written directly to response stream (no file system dependency)
 *    - No temporary files created on disk
 * 
 * 4. IN-MEMORY PROCESSING:
 *    - Excel workbook is created in-memory
 *    - Written directly to response output stream
 *    - No file system dependency (cloud-friendly)
 *    - Compatible with ephemeral container storage
 *    - Works in read-only file systems
 * 
 * USAGE PATTERN:
 * ==============
 * Controllers should pass all required data through the model:
 * 
 *   @GetMapping("/export/users/excel")
 *   public String exportUsersExcel(Model model) {
 *       List<User> users = userService.listAllUsers();
 *       model.addAttribute("users", users);
 *       return "excelView";
 *   }
 * 
 * DO NOT use session.setAttribute() for view data.
 * 
 * DISTRIBUTED SESSION MANAGEMENT:
 * ===============================
 * If session data is needed for authentication/authorization:
 * - Session is managed by Spring Session + Redis (AWS ElastiCache)
 * - Session data persists across instance restarts
 * - Multiple instances share session state transparently
 * - But view data should ALWAYS come from model, not session
 */
public class ExcelView extends AbstractXlsView{

    /**
     * Builds Excel document from model data in a stateless manner.
     * 
     * CLOUD-NATIVE IMPLEMENTATION:
     * ============================
     * - All user data is retrieved from the model parameter (not from session)
     * - Excel workbook is created in-memory
     * - Workbook is written directly to the HTTP response stream
     * - No file system dependency (no temporary files)
     * - No server-side state is created or accessed
     * - Compatible with distributed cloud environments
     * - Works in read-only file systems (container best practice)
     * 
     * PROCESS FLOW:
     * 1. Retrieve user list from model (passed by controller)
     * 2. Set response headers for file download
     * 3. Create Excel workbook in-memory
     * 4. Populate workbook with user data
     * 5. Write workbook directly to response stream
     * 6. No temporary files or session state involved
     * 
     * @param model Contains "users" attribute with List<User> data
     * @param workbook In-memory Excel workbook to populate
     * @param request HTTP request (not used for session access)
     * @param response HTTP response (Excel is written directly to output stream)
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // change the file name
        response.setHeader("Content-Disposition", "attachment; filename=\"my-xls-file.xls\"");

        // Retrieve data from model (not from session) - cloud-native stateless pattern
        // This ensures the view works correctly in distributed cloud environments
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Validate that data is present (fail fast if controller didn't provide data)
        if (users == null) {
            throw new IllegalStateException("No 'users' data found in model. " +
                "Controller must add users to model: model.addAttribute(\"users\", userList)");
        }

        // create excel xls sheet
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

        // Populate rows with user data from model (not from session)
        // All data is passed through the model parameter for cloud compatibility
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
