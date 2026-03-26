package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
/**
 * Abstract CSV View for generating CSV exports
 * 
 * Cloud-Native Considerations:
 * - This view uses HttpServletRequest/Response but does NOT store state in HTTP session
 * - Session state is managed by Spring Session Redis (configured in RedisSessionConfig)
 * - The view is stateless and can be used across multiple instances
 * - All session data is externalized to Redis for horizontal scaling
 * - Compatible with cloud load balancers and auto-scaling groups
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
