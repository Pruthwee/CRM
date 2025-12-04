package crm.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PdfViewTest {

    @Test
    public void testPdfViewConstructor() {
        PdfView pdfView = new PdfView();
        assertNotNull(pdfView);
    }

    @Test
    public void testPdfViewContentType() {
        PdfView pdfView = new PdfView();
        assertEquals("application/pdf", pdfView.getContentType());
    }
}
