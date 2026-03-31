package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract base class for CSV views.
 * 
 * CLOUD-READY: This view is stateless and does not rely on HTTP session state.
 * All data required for rendering is passed through the model parameter.
 * This ensures horizontal scalability and cloud-native compatibility.
 * 
 * Note: HttpServletRequest is only used for reading request metadata (headers, parameters),
 * NOT for accessing session state. All application state is passed via the model.
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
     * STATELESS DESIGN: This method does NOT access HTTP session state.
     * All required data must be provided in the model parameter.
     * The request parameter is only used for reading request metadata if needed.
     * 
     * @param model Contains all data needed for rendering (stateless)
     * @param request Used only for request metadata, NOT session access
     * @param response Target for CSV output
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        
        // Ensure no session state is accessed - all data comes from model
        buildCsvDocument(model, request, response);
    }

    /**
     * Subclasses must implement this to build the CSV document.
     * 
     * IMPORTANT: Do NOT access request.getSession() or any session state.
     * All data must come from the model parameter to maintain stateless behavior.
     * 
     * @param model Contains all data needed for CSV generation
     * @param request For request metadata only (NOT session access)
     * @param response Target for CSV output
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
