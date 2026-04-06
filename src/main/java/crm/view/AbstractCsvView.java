package crm.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Cloud-ready abstract CSV view for Spring MVC.
 * Compatible with embedded servlet containers (Tomcat, Jetty, Undertow).
 * Suitable for containerized deployments and cloud platforms.
 */
public abstract class AbstractCsvView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(AbstractCsvView.class);
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

    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.setContentType(getContentType());
        
        try {
            buildCsvDocument(model, request, response);
            log.debug("CSV document rendered successfully");
        } catch (Exception e) {
            log.error("Error rendering CSV document: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Build CSV document content.
     * Subclasses must implement this method to generate CSV output.
     * 
     * @param model the model map
     * @param request current HTTP request
     * @param response current HTTP response
     * @throws Exception if document generation fails
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
