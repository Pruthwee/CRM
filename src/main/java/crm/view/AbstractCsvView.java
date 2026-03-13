package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Cloud-Ready Abstract CSV View
 * - Stateless design for horizontal scaling
 * - No session state dependencies
 * - Compatible with cloud load balancers
 * - Supports distributed session management
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
     * Cloud-Ready: Stateless rendering without session dependencies
     * Uses only request parameters and model data for rendering
     * Compatible with distributed session stores (Redis, Hazelcast)
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // Set content type for cloud-compatible response
        response.setContentType(getContentType());
        
        // Set cache control headers for cloud CDN compatibility
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Build CSV document using only model data (stateless)
        buildCsvDocument(model, request, response);
    }

    /**
     * Build CSV document from model data
     * Implementation should be stateless and not rely on session state
     * 
     * @param model Request model containing data to render
     * @param request HTTP request (use only for request parameters, not session)
     * @param response HTTP response for writing CSV output
     * @throws Exception if document generation fails
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;
}
