package crm.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractPdfViewTest {

    // Concrete subclass for testing the abstract class
    private static class ConcretePdfView extends AbstractPdfView {
        @Override
        protected void buildPdfDocument(
                java.util.Map<String, Object> model,
                com.itextpdf.text.Document document,
                com.itextpdf.text.pdf.PdfWriter writer,
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response) throws Exception {
            // no-op for testing
        }
    }

    private ConcretePdfView pdfView;

    @BeforeEach
    void setUp() {
        pdfView = new ConcretePdfView();
    }

    @Test
    void testConstructor_setsContentType() {
        assertEquals("application/pdf", pdfView.getContentType());
    }

    @Test
    void testGeneratesDownloadContent_returnsTrue() {
        assertTrue(pdfView.generatesDownloadContent());
    }

    @Test
    void testInstantiation_notNull() {
        assertNotNull(pdfView);
    }

    @Test
    void testContentType_isPdf() {
        String contentType = pdfView.getContentType();
        assertTrue(contentType.contains("pdf"));
    }

    @Test
    void testGetViewerPreferences_returnsNonZero() {
        int prefs = pdfView.getViewerPreferences();
        assertTrue(prefs > 0);
    }
}
