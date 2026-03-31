package crm.view;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Abstract base class for PDF views in a cloud-native stateless architecture.
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
 *    - PDF generation is in-memory using ByteArrayOutputStream
 *    - No file system dependency (cloud-friendly)
 * 
 * 4. DISTRIBUTED SESSION MANAGEMENT:
 *    - If session data is needed, it's stored in Redis (AWS ElastiCache)
 *    - Session data persists across instance restarts
 *    - Multiple instances share session state transparently
 * 
 * 5. IN-MEMORY PROCESSING:
 *    - Uses ByteArrayOutputStream for PDF generation
 *    - No temporary files created on disk
 *    - Compatible with ephemeral container storage
 *    - Works in read-only file systems
 * 
 * USAGE PATTERN:
 * ==============
 * Controllers should pass all required data through the model:
 * 
 *   @GetMapping("/export/pdf")
 *   public String exportPdf(Model model) {
 *       model.addAttribute("data", dataService.getData());
 *       return "pdfView";
 *   }
 * 
 * DO NOT use session.setAttribute() for view data.
 */
public abstract class AbstractPdfView extends AbstractView {

    /**
     * This constructor sets the appropriate content type "application/pdf".
     * Note that IE won't take much notice of this, but there's not a lot we
     * can do about this. Generated documents should have a ".pdf" extension.
     */
    public AbstractPdfView() {
        setContentType("application/pdf");
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    /**
     * Renders PDF content in a stateless manner using in-memory processing.
     * 
     * CLOUD-NATIVE IMPLEMENTATION:
     * ============================
     * - All data is provided through the model parameter (not session)
     * - PDF is generated in-memory using ByteArrayOutputStream
     * - No file system dependency (compatible with ephemeral storage)
     * - No server-side state is created or accessed
     * - Compatible with distributed cloud environments
     * - Works in read-only file systems (container best practice)
     * 
     * PROCESS FLOW:
     * 1. Create in-memory ByteArrayOutputStream
     * 2. Generate PDF document in memory
     * 3. Write completed PDF to HTTP response stream
     * 4. No temporary files or session state involved
     * 
     * @param model Contains all data needed for PDF generation (passed by controller)
     * @param request HTTP request (used for headers only, not session access)
     * @param response HTTP response (PDF is written directly to output stream)
     */
    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception  {

        // IE workaround: write into byte array first (also cloud-friendly - no file system dependency).
        ByteArrayOutputStream baos = createTemporaryOutputStream();

        // Apply preferences and build metadata.
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        prepareWriter(model, writer, request);
        buildPdfMetadata(model, document, request);

        // Build PDF document.
        document.open();
        buildPdfDocument(model, document, writer, request, response);
        document.close();

        // Flush to HTTP response.
        writeToResponse(response, baos);
    }

    /**
     * Prepare the given PdfWriter. Called before building the PDF document,
     * that is, before the call to {@code Document.open()}.
     * <p>Useful for registering a page event listener, for example.
     * The default implementation sets the viewer preferences as returned
     * by this class's {@code getViewerPreferences()} method.
     * @param model the model, in case meta information must be populated from it
     * @param writer the PdfWriter to prepare
     * @param request in case we need locale etc. Shouldn't look at attributes.
     * @throws DocumentException if thrown during writer preparation
     */
    protected void prepareWriter(Map<String, Object> model, PdfWriter writer, HttpServletRequest request) throws DocumentException {
        writer.setViewerPreferences(getViewerPreferences());
    }

    /**
     * Return the viewer preferences for the PDF file.
     * <p>By default returns {@code AllowPrinting} and
     * {@code PageLayoutSinglePage}, but can be subclassed.
     * The subclass can either have fixed preferences or retrieve
     * them from bean properties defined on the View.
     * @return an int containing the bits information against PdfWriter definitions
     */
    protected int getViewerPreferences() {
        return PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage;
    }

    /**
     * Populate the iText Document's meta fields (author, title, etc.).
     * <br>Default is an empty implementation. Subclasses may override this method
     * to add meta fields such as title, subject, author, creator, keywords, etc.
     * This method is called after assigning a PdfWriter to the Document and
     * before calling {@code document.open()}.
     * @param model the model, in case meta information must be populated from it
     * @param document the iText document being populated
     * @param request in case we need locale etc. Shouldn't look at attributes.
     */
    protected void buildPdfMetadata(Map<String, Object> model, Document document, HttpServletRequest request) {
    }

    /**
     * Subclasses must implement this method to build an iText PDF document,
     * given the model. Called between {@code Document.open()} and
     * {@code Document.close()} calls.
     * <p>Note that the passed-in HTTP response is just supposed to be used
     * for setting cookies or other HTTP headers. The built PDF document itself
     * will automatically get written to the response after this method returns.
     * 
     * CLOUD-NATIVE IMPLEMENTATION REQUIREMENTS:
     * ==========================================
     * 1. Retrieve all data from the 'model' parameter
     * 2. DO NOT call request.getSession().getAttribute()
     * 3. DO NOT write to file system (use in-memory streams only)
     * 4. Ensure method is stateless (no instance variable modifications)
     * 5. All PDF content is generated in-memory
     * 
     * Example implementation:
     * 
     *   @Override
     *   protected void buildPdfDocument(Map<String, Object> model, 
     *                                   Document document, 
     *                                   PdfWriter writer,
     *                                   HttpServletRequest request, 
     *                                   HttpServletResponse response) throws Exception {
     *       List<Data> data = (List<Data>) model.get("data");
     *       PdfPTable table = new PdfPTable(3);
     *       // Add PDF content...
     *       document.add(table);
     *   }
     * 
     * @param model the model Map (contains all data from controller)
     * @param document the iText Document to add elements to
     * @param writer the PdfWriter to use
     * @param request in case we need locale etc. Shouldn't look at attributes.
     * @param response in case we need to set cookies. Shouldn't write to it.
     * @throws Exception any exception that occurred during document building
     */
    protected abstract void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
                                             HttpServletRequest request, HttpServletResponse response) throws Exception;
}
