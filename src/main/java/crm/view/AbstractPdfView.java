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
 * Abstract PDF View - Cloud-Native Stateless Implementation
 * 
 * This view has been refactored to be stateless and cloud-ready:
 * - No session state storage
 * - All data passed through model (request-scoped)
 * - Compatible with horizontal scaling
 * - Works with distributed session stores (Redis/Hazelcast)
 * - Uses in-memory byte streams (no file system dependencies)
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
     * Renders the PDF document in a stateless manner.
     * All data is passed through the model parameter (request-scoped).
     * No session state is stored or accessed.
     * Uses in-memory byte streams to avoid file system dependencies.
     * 
     * @param model Request-scoped model data
     * @param request HTTP request (used only for reading request parameters, not for storing state)
     * @param response HTTP response (used only for writing output)
     */
    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception  {

        // IE workaround: write into byte array first (in-memory, no file system dependency)
        ByteArrayOutputStream baos = createTemporaryOutputStream();

        // Apply preferences and build metadata using only request-scoped data
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        // Prepare writer with request-scoped data only (no session state)
        prepareWriter(model, writer, request);
        
        // Build metadata with request-scoped data only (no session state)
        buildPdfMetadata(model, document, request);

        // Build PDF document using only request-scoped data from model
        document.open();
        buildPdfDocument(model, document, writer, request, response);
        document.close();

        // Flush to HTTP response (stateless operation)
        writeToResponse(response, baos);
    }

    /**
     * Prepare the given PdfWriter. Called before building the PDF document,
     * that is, before the call to {@code Document.open()}.
     * <p>Useful for registering a page event listener, for example.
     * The default implementation sets the viewer preferences as returned
     * by this class's {@code getViewerPreferences()} method.
     * 
     * Implementation must be stateless - use only data from the model parameter.
     * Do not store or retrieve data from HTTP session.
     * 
     * @param model Request-scoped model data (stateless)
     * @param writer the PdfWriter to prepare
     * @param request HTTP request (read-only, for request parameters only)
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
     * Implementation must be stateless - use only data from the model parameter.
     * Do not store or retrieve data from HTTP session.
     * 
     * @param model Request-scoped model data (stateless)
     * @param document the iText document being populated
     * @param request HTTP request (read-only, for request parameters only)
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
     * Implementation must be stateless - use only data from the model parameter.
     * Do not store or retrieve data from HTTP session.
     * 
     * @param model Request-scoped model data (stateless)
     * @param document the iText Document to add elements to
     * @param writer the PdfWriter to use
     * @param request HTTP request (read-only, for request parameters only)
     * @param response HTTP response (write-only, for headers and cookies only)
     * @throws Exception any exception that occurred during document building
     */
    protected abstract void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
                                             HttpServletRequest request, HttpServletResponse response) throws Exception;
}
