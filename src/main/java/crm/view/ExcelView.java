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
 * Excel view implementation for exporting User data.
 * 
 * CLOUD-READY: This view is stateless and does not access HTTP session state.
 * All data is passed through the model parameter, ensuring horizontal scalability
 * and compatibility with cloud environments (AWS, Azure, GCP).
 * 
 * The view can be safely load-balanced across multiple instances without
 * session affinity requirements.
 */
public class ExcelView extends AbstractXlsView{

    /**
     * Builds the Excel document from the model data.
     * 
     * STATELESS IMPLEMENTATION: This method retrieves all data from the model parameter.
     * No session state is accessed, ensuring cloud-native stateless behavior.
     * 
     * @param model Contains the "users" list - all data needed for Excel generation
     * @param workbook The Excel workbook to populate
     * @param request Not used for session access - only for potential request metadata
     * @param response Target for Excel output with appropriate headers
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // Set response headers for file download
        response.setHeader("Content-Disposition", "attachment; filename=\"my-xls-file.xls\"");

        // Retrieve data from model (stateless) - NOT from session
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) model.get("users");
        
        // Validate that required data is present in model
        if (users == null) {
            throw new IllegalStateException("Required 'users' data not found in model. " +
                    "Ensure all data is passed via model for stateless operation.");
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

        // Write data rows - all from model (stateless)
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
