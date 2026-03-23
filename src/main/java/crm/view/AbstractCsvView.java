package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract CSV View - Cloud-Native Stateless Implementation
 * 
 * This view has been refactored to be stateless and cloud-ready:
 * - No session state storage
 * - All data passed through model (request-scoped)
 * - Compatible with horizontal scaling
 * - Works with distributed session stores (Redis/Hazelcast)
 */
public abstract class AbstractCsvView extends AbstractView {

    private static final String CONTENT_TYPE = "text/csv";

    // Removed instance variable 'url' to make class stateless
    // If URL is needed, it should be passed through the model

    public AbstractCsvView() {
        setContentType(CONTENT_TYPE);
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    /**
     * Renders the CSV document in a stateless manner.
     * All data is passed through the model parameter (request-scoped).
     * No session state is stored or accessed.
     * 
     * @param model Request-scoped model data
     * @param request HTTP request (used only for reading request parameters, not for storing state)
     * @param response HTTP response (used only for writing output)
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // Set content type for CSV download
        response.setContentType(getContentType());
        
        // Build CSV document using only request-scoped data from model
        // No session state is accessed or modified
        buildCsvDocument(model, request, response);
    }

    /**
     * Subclasses implement this method to build CSV content.
     * Implementation must be stateless - use only data from the model parameter.
     * Do not store or retrieve data from HTTP session.
     * 
     * @param model Request-scoped model data (stateless)
     * @param request HTTP request (read-only, for request parameters only)
     * @param response HTTP response (write-only, for output stream)
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
