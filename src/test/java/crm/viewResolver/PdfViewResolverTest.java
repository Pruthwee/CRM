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
    void testDefaultConstructor() {
        PdfViewResolver resolver = new PdfViewResolver();
        assertNotNull(resolver);
    }

    @Test
    void testResolveViewName_returnsNonNull() throws Exception {
        View view = pdfViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_returnsPdfView() throws Exception {
        View view = pdfViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertTrue(view instanceof PdfView);
    }

    @Test
    void testResolveViewName_withNullViewName() throws Exception {
        View view = pdfViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_withDifferentLocale() throws Exception {
        View view = pdfViewResolver.resolveViewName("test", Locale.GERMAN);
        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    void testResolveViewName_withEmptyViewName() throws Exception {
        View view = pdfViewResolver.resolveViewName("", Locale.ENGLISH);
        assertNotNull(view);
    }
}
