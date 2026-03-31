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
 * Abstract base class for PDF views using iText.
 * 
 * CLOUD-NATIVE STATELESS DESIGN:
 * This view implementation is designed to be stateless and cloud-ready.
 * - Does NOT store any state in HTTP session
 * - Does NOT rely on server-side session affinity
 * - All data is passed via the model (request-scoped)
 * - PDF generation is performed in-memory without file system dependencies
 * - Safe for horizontal scaling across multiple instances
 * 
 * Session Management:
 * If session data is needed, it is now managed by Spring Session with Redis,
 * allowing stateless application instances that can scale horizontally.
 * 
 * Cloud Deployment Notes:
 * - PDF is generated in-memory using ByteArrayOutputStream
 * - No temporary files are created on the file system
 * - No server-side state is maintained between requests
 * - Compatible with AWS ECS, EKS, Lambda, and other cloud platforms
 * - Works correctly behind load balancers without sticky sessions
 * - Memory-efficient for cloud environments with proper resource limits
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
     * Renders the PDF document.
     * 
     * STATELESS OPERATION:
     * This method receives all necessary data via the model parameter.
     * It does NOT access or modify HTTP session state.
     * All processing is request-scoped and stateless.
     * PDF is generated in-memory without file system dependencies.
     * 
     * @param model Request-scoped model data (not session data)
     * @param request HTTP request (used only for reading request parameters, not session)
     * @param response HTTP response for writing PDF output
     */
    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception  {

        // IE workaround: write into byte array first.
        // This also ensures stateless operation - no file system dependencies
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
     * 
     * STATELESS OPERATION:
     * - Use only data from the model parameter (request-scoped)
     * - Do NOT access request.getSession() or store session state
     * 
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
     * 
     * STATELESS OPERATION:
     * - Use only data from the model parameter (request-scoped)
     * - Do NOT access request.getSession() or store session state
     * 
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
     * IMPLEMENTATION GUIDELINES:
     * - Use only data from the model parameter (request-scoped)
     * - Do NOT access request.getSession() or store session state
     * - Keep all processing stateless and request-scoped
     * - If user-specific data is needed, pass it via the model
     * - Do NOT write to file system - all processing is in-memory
     * 
     * @param model the model Map
     * @param document the iText Document to add elements to
     * @param writer the PdfWriter to use
     * @param request in case we need locale etc. Shouldn't look at attributes.
     * @param response in case we need to set cookies. Shouldn't write to it.
     * @throws Exception any exception that occurred during document building
     */
    protected abstract void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
                                             HttpServletRequest request, HttpServletResponse response) throws Exception;
}
