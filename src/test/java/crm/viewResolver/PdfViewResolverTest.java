package crm.viewResolver;

import crm.view.PdfView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class PdfViewResolverTest {

    private PdfViewResolver pdfViewResolver;

    @BeforeEach
    public void setUp() {
        pdfViewResolver = new PdfViewResolver();
    }

    @Test
    public void testResolveViewName() throws Exception {
        View view = pdfViewResolver.resolveViewName("testView", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    public void testResolveViewNameWithNullViewName() throws Exception {
        View view = pdfViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    public void testResolveViewNameWithNullLocale() throws Exception {
        View view = pdfViewResolver.resolveViewName("testView", null);
        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    public void testResolveViewNameReturnsPdfView() throws Exception {
        View view = pdfViewResolver.resolveViewName("anyView", Locale.US);
        assertEquals(PdfView.class, view.getClass());
    }

    @Test
    public void testResolveViewNameWithDifferentLocales() throws Exception {
        View view1 = pdfViewResolver.resolveViewName("view1", Locale.FRENCH);
        View view2 = pdfViewResolver.resolveViewName("view2", Locale.GERMAN);

        assertNotNull(view1);
        assertNotNull(view2);
        assertTrue(view1 instanceof PdfView);
        assertTrue(view2 instanceof PdfView);
    }

    @Test
    public void testViewResolverNotNull() {
        assertNotNull(pdfViewResolver);
    }
}
