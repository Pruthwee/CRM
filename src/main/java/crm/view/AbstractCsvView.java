package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Cloud-ready Abstract CSV View
 * - Stateless design (no session dependencies)
 * - Compatible with horizontal scaling
 * - Works with cloud load balancers
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
     * Cloud-ready: Stateless rendering without session dependencies
     * All data passed through model, not stored in session
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        // Stateless operation - no session access
        buildCsvDocument(model, request, response);
    }

    /**
     * Build CSV document from model data
     * Cloud-ready: Stateless operation
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
