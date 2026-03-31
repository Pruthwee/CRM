package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract CSV View for Cloud-Native Export
 * 
 * Cloud Readiness Improvements:
 * - Stateless design: No session state dependencies
 * - Request-scoped data only: All data passed via model
 * - No server affinity: Can run on any instance
 * - Horizontal scaling ready: No instance-specific state
 * 
 * This view generates CSV content directly from request-scoped model data
 * without relying on HTTP session state, making it compatible with
 * distributed session stores (Redis/Hazelcast) and cloud load balancers.
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
     * Render CSV content from model data (stateless operation)
     * 
     * Cloud-Native Pattern:
     * - All data comes from the model parameter (request-scoped)
     * - No session state access or modification
     * - Response written directly to output stream
     * - No server-side state persistence
     * 
     * This ensures the view can be executed on any application instance
     * in a horizontally scaled cloud environment.
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        
        // Build CSV document using only request-scoped model data
        // No session state access - fully stateless operation
        buildCsvDocument(model, request, response);
    }

    /**
     * Build CSV document from model data
     * 
     * Implementations must be stateless and use only:
     * - Data from the model parameter
     * - Request parameters (read-only)
     * - Response for writing output
     * 
     * Do NOT access or modify HTTP session state
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
