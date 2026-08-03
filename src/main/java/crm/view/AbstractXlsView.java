package crm.view;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;

/**
 * Custom AbstractXlsView to replace the removed Spring Framework class.
 * This class provides Excel view support for Spring Boot 3.x applications.
 */
public abstract class AbstractXlsView extends AbstractView {

    private static final String CONTENT_TYPE = "application/vnd.ms-excel";

    public AbstractXlsView() {
        setContentType(CONTENT_TYPE);
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // Create a new workbook
        Workbook workbook = createWorkbook();
        
        // Build the Excel document
        buildExcelDocument(model, workbook, request, response);
        
        // Set the content type
        response.setContentType(getContentType());
        
        // Write the workbook to the response
        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        workbook.close();
    }

    /**
     * Create a new Workbook instance. Default implementation creates an HSSFWorkbook (XLS format).
     * Subclasses can override this to create different workbook types.
     * 
     * @return a new Workbook instance
     */
    protected Workbook createWorkbook() {
        return new HSSFWorkbook();
    }

    /**
     * Subclasses must implement this method to populate the Excel workbook.
     * 
     * @param model the model Map
     * @param workbook the Excel workbook to populate
     * @param request the current HTTP request
     * @param response the current HTTP response
     * @throws Exception if an error occurs during document building
     */
    protected abstract void buildExcelDocument(
            Map<String, Object> model, Workbook workbook,
            HttpServletRequest request, HttpServletResponse response) throws Exception;
}
