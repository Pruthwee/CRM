package crm.viewResolver;

import crm.view.PdfView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class PdfViewResolverTest {

    private PdfViewResolver pdfViewResolver;

    @BeforeEach
    void setUp() {
        pdfViewResolver = new PdfViewResolver();
    }

    @Test
    void testConstructor() {
        assertNotNull(pdfViewResolver);
    }

    @Test
    void testResolveViewName() throws Exception {
        View view = pdfViewResolver.resolveViewName("test", Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    void testResolveViewNameWithNullString() throws Exception {
        View view = pdfViewResolver.resolveViewName(null, Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    void testResolveViewNameWithEmptyString() throws Exception {
        View view = pdfViewResolver.resolveViewName("", Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    void testResolveViewNameWithNullLocale() throws Exception {
        View view = pdfViewResolver.resolveViewName("test", null);

        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    void testResolveViewNameReturnsPdfView() throws Exception {
        View view = pdfViewResolver.resolveViewName("anyName", Locale.US);

        assertTrue(view instanceof PdfView);
    }
}
