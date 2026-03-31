package crm.view;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Abstract base class for CSV views in a cloud-native stateless architecture.
 * 
 * CLOUD READINESS IMPLEMENTATION (AWS Compatible):
 * ================================================
 * This view is fully stateless and cloud-ready for horizontal scaling:
 * 
 * 1. NO HTTP SESSION DEPENDENCIES:
 *    - Does NOT access HttpSession.getAttribute() or setAttribute()
 *    - All data is passed through the model parameter from controllers
 *    - Session state (if needed) is managed externally by Spring Session + Redis
 * 
 * 2. STATELESS DESIGN PRINCIPLES:
 *    - No instance variables store request-specific data
 *    - All data flows through method parameters (model, request, response)
 *    - Each request is independent and can be handled by any instance
 *    - No server affinity required for load balancing
 * 
 * 3. CLOUD-NATIVE PATTERNS:
 *    - Compatible with AWS ECS, EKS, Elastic Beanstalk
 *    - Supports auto-scaling without data loss
 *    - Works with Application Load Balancer (no sticky sessions needed)
 *    - CSV generation is in-memory (no file system dependency)
 * 
 * 4. DISTRIBUTED SESSION MANAGEMENT:
 *    - If session data is needed, it's stored in Redis (AWS ElastiCache)
 *    - Session data persists across instance restarts
 *    - Multiple instances share session state transparently
 * 
 * USAGE PATTERN:
 * ==============
 * Controllers should pass all required data through the model:
 * 
 *   @GetMapping("/export/csv")
 *   public String exportCsv(Model model) {
 *       model.addAttribute("data", dataService.getData());
 *       return "csvView";
 *   }
 * 
 * DO NOT use session.setAttribute() for view data.
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
     * 
     * CLOUD-NATIVE IMPLEMENTATION:
     * - All data is provided through the model parameter (not session)
     * - CSV is written directly to response output stream (no file system)
     * - No server-side state is created or accessed
     * - Compatible with distributed cloud environments
     * 
     * @param model Contains all data needed for CSV generation (passed by controller)
     * @param request HTTP request (used for headers only, not session access)
     * @param response HTTP response (CSV is written directly to output stream)
     */
    @Override
    protected final void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        buildCsvDocument(model, request, response);
    }

    /**
     * Build the CSV document from model data.
     * 
     * IMPLEMENTATION REQUIREMENTS FOR CLOUD READINESS:
     * =================================================
     * 1. Retrieve all data from the 'model' parameter
     * 2. DO NOT call request.getSession().getAttribute()
     * 3. Write CSV directly to response.getWriter() or response.getOutputStream()
     * 4. Do NOT write to file system (use in-memory streams only)
     * 5. Ensure method is stateless (no instance variable modifications)
     * 
     * Example implementation:
     * 
     *   @Override
     *   protected void buildCsvDocument(Map<String, Object> model, 
     *                                   HttpServletRequest request, 
     *                                   HttpServletResponse response) throws Exception {
     *       List<Data> data = (List<Data>) model.get("data");
     *       CSVWriter writer = new CSVWriter(response.getWriter());
     *       // Write CSV data...
     *       writer.close();
     *   }
     * 
     * @param model Contains all data for CSV generation (from controller)
     * @param request HTTP request (for headers, NOT for session access)
     * @param response HTTP response (write CSV directly to output stream)
     */
    protected abstract void buildCsvDocument(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

}
