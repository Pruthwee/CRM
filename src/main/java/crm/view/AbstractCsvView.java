package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract base class for CSV views in a cloud-native stateless architecture.
 * 
 * CLOUD READINESS NOTES:
 * - This view is designed to be stateless and does not store data in HTTP session
 * - All data is passed through the model parameter, not session attributes
 * - Session data (if needed) is managed by Spring Session with Redis for distributed storage
 * - This enables horizontal scaling and load balancing without sticky sessions
 * - Compatible with AWS, Azure, and GCP cloud environments
 * 
 * STATELESS DESIGN:
 * - No instance variables store request-specific data
 * - All data flows through method parameters (model, request, response)
 * - No dependency on server-side session state
 * - Each request is independent and can be handled by any instance
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
     * Renders CSV content in a stateless manner.
     * All data is provided through the model parameter, not session storage.
     * This ensures compatibility with distributed cloud environments.
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        buildCsvDocument(model, request, response);
    }

    /**
     * Build the CSV document from model data.
     * Implementations should NOT access session state directly.
     * All required data should be passed through the model parameter.
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
