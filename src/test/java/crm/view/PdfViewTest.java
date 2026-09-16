package crm.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfViewTest {

    private PdfView pdfView;

    @BeforeEach
    void setUp() {
        pdfView = new PdfView();
    }

    @Test
    void testDefaultConstructor() {
        PdfView view = new PdfView();
        assertNotNull(view);
    }

    @Test
    void testContentType_isPdf() {
        assertEquals("application/pdf", pdfView.getContentType());
    }

    @Test
    void testGeneratesDownloadContent_returnsTrue() {
        assertTrue(pdfView.generatesDownloadContent());
    }

    @Test
    void testIsInstanceOfAbstractPdfView() {
        assertTrue(pdfView instanceof AbstractPdfView);
    }

    @Test
    void testGetViewerPreferences_returnsNonZero() {
        int prefs = pdfView.getViewerPreferences();
        assertTrue(prefs > 0);
    }
}
