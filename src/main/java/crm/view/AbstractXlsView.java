package crm.view;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;

/**
 * Custom AbstractXlsView implementation to replace the deprecated Spring Framework class.
 * This class provides Excel (.xls) file generation support for Spring MVC views.
 */
public abstract class AbstractXlsView extends AbstractView {

    /**
     * Default Constructor. Sets the content type of the view to "application/vnd.ms-excel".
     */
    public AbstractXlsView() {
        setContentType("application/vnd.ms-excel");
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                                  HttpServletResponse response) throws Exception {
        // Create a new workbook
        Workbook workbook = createWorkbook();

        // Let subclass build the Excel document
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
     * Creates a new Workbook instance for the Excel document.
     * Default implementation creates an HSSFWorkbook (for .xls format).
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
     * @param request in case we need locale etc. Shouldn't look at attributes.
     * @param response in case we need to set cookies. Shouldn't write to it.
     * @throws Exception any exception that occurred during document building
     */
    protected abstract void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                               HttpServletRequest request, HttpServletResponse response) throws Exception;
}
