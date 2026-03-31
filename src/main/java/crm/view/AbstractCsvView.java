package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract base class for CSV views.
 * 
 * CLOUD-NATIVE STATELESS DESIGN:
 * This view implementation is designed to be stateless and cloud-ready.
 * - Does NOT store any state in HTTP session
 * - Does NOT rely on server-side session affinity
 * - All data is passed via the model (request-scoped)
 * - Safe for horizontal scaling across multiple instances
 * 
 * Session Management:
 * If session data is needed, it is now managed by Spring Session with Redis,
 * allowing stateless application instances that can scale horizontally.
 * 
 * Cloud Deployment Notes:
 * - This view generates CSV content on-the-fly from model data
 * - No server-side state is maintained between requests
 * - Compatible with AWS ECS, EKS, Lambda, and other cloud platforms
 * - Works correctly behind load balancers without sticky sessions
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
     * Renders the CSV document.
     * 
     * STATELESS OPERATION:
     * This method receives all necessary data via the model parameter.
     * It does NOT access or modify HTTP session state.
     * All processing is request-scoped and stateless.
     * 
     * @param model Request-scoped model data (not session data)
     * @param request HTTP request (used only for reading request parameters, not session)
     * @param response HTTP response for writing CSV output
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        buildCsvDocument(model, request, response);
    }

    /**
     * Subclasses must implement this method to build the CSV document.
     * 
     * IMPLEMENTATION GUIDELINES:
     * - Use only data from the model parameter (request-scoped)
     * - Do NOT access request.getSession() or store session state
     * - Keep all processing stateless and request-scoped
     * - If user-specific data is needed, pass it via the model
     * 
     * @param model Request-scoped model data
     * @param request HTTP request (for request parameters only)
     * @param response HTTP response for CSV output
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
