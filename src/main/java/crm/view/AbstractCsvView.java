package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract base class for CSV views.
 * 
 * CLOUD-NATIVE PATTERN:
 * This view class has been updated to work with distributed session management.
 * Session data is now stored in Redis (configured via RedisSessionConfig),
 * enabling stateless horizontal scaling in cloud environments.
 * 
 * The HttpServletRequest and HttpServletResponse are used only for:
 * - Reading request parameters (stateless)
 * - Writing response data (stateless)
 * - Session data is automatically managed by Spring Session Redis
 * 
 * No direct session manipulation is performed in this class, making it
 * compatible with cloud-native stateless architectures.
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
     * CLOUD-READY: This method uses request/response objects in a stateless manner.
     * Any session data accessed through the request is automatically managed by
     * Spring Session Redis, ensuring compatibility with cloud environments.
     * 
     * @param model the model data
     * @param request the HTTP request (session managed by Redis)
     * @param response the HTTP response
     * @throws Exception if rendering fails
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
     * CLOUD-READY: Implementations should avoid storing state in instance variables.
     * All data should be passed through the model or retrieved from the distributed
     * session store (Redis) via the request object.
     * 
     * @param model the model data
     * @param request the HTTP request (session managed by Redis)
     * @param response the HTTP response
     * @throws Exception if document building fails
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
