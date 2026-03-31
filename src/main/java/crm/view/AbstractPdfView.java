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
 * CLOUD READINESS NOTES:
 * - This view is designed to be stateless and does not store data in HTTP session
 * - All data is passed through the model parameter, not session attributes
 * - Session data (if needed) is managed by Spring Session with Redis for distributed storage
 * - This enables horizontal scaling and load balancing without sticky sessions
 * - Compatible with AWS, Azure, and GCP cloud environments
 * 
 * STATELESS DESIGN:
 * - No instance variables store request-specific data
 * - All data flows through method parameters (model, request, response)
 * - No dependency on server-side session state
 * - Each request is independent and can be handled by any instance
 * - PDF generation uses in-memory ByteArrayOutputStream (no file system dependency)
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
     * All data is provided through the model parameter, not session storage.
     * This ensures compatibility with distributed cloud environments.
     * 
     * Uses ByteArrayOutputStream for in-memory PDF generation to avoid
     * file system dependencies in cloud environments.
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
     * CLOUD-NATIVE IMPLEMENTATION NOTES:
     * - Do NOT access session attributes directly (use model data instead)
     * - Do NOT write to file system (use in-memory streams)
     * - All required data should be passed through the model parameter
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
