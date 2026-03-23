package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
/**
 * Abstract CSV View for Cloud-Native Export Functionality
 * 
 * CLOUD READINESS: This view is now stateless and cloud-ready.
 * - Sessions are managed by Redis (distributed session store)
 * - No server affinity required - works with load balancers
 * - Horizontally scalable across multiple instances
 * - All session data persists in external Redis store
 * 
 * The view receives data through the model (stateless) and generates CSV responses.
 */

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
        buildCsvDocument(model, request, response);
    }

    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
