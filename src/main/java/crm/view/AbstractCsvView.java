package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract base class for CSV views in cloud-native environment.
 * 
 * Cloud-Ready Improvements:
 * - Stateless design: No session state storage
 * - All data passed through model Map (request-scoped)
 * - Response written directly to output stream (no file system dependency)
 * - Compatible with horizontal scaling and load balancing
 * 
 * This view generates CSV content on-the-fly without storing state,
 * making it suitable for cloud deployment where instances can be
 * terminated or scaled at any time.
 */
public abstract class AbstractCsvView extends AbstractView {

    private static final String CONTENT_TYPE = "text/csv";

    private String url;

    public AbstractCsvView() {
        setContentType(CONTENT_TYPE);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    /**
     * Renders the CSV view by delegating to buildCsvDocument.
     * 
     * Cloud-Native Pattern:
     * - Data is passed via model Map (stateless, request-scoped)
     * - No session state is accessed or stored
     * - Response is streamed directly to client
     * - No file system operations (cloud-compatible)
     * 
     * @param model Request-scoped data model
     * @param request HTTP request (used only for request metadata, not session)
     * @param response HTTP response for streaming output
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        
        // Build CSV document directly to response stream (stateless operation)
        buildCsvDocument(model, request, response);
    }

    /**
     * Subclasses implement this method to generate CSV content.
     * 
     * Implementation Guidelines for Cloud Compatibility:
     * - Use only data from the model Map (request-scoped)
     * - Do NOT access HttpSession
     * - Do NOT store state in instance variables
     * - Write directly to response.getWriter() or response.getOutputStream()
     * - Do NOT write to local file system
     * 
     * @param model Request-scoped data containing objects to export
     * @param request HTTP request (for metadata only, not session access)
     * @param response HTTP response for streaming CSV output
     * @throws Exception if CSV generation fails
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;
}
