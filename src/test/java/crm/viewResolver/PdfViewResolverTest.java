package crm.viewResolver;

import crm.view.PdfView;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class PdfViewResolverTest {

    @Test
    public void testPdfViewResolverConstructor() {
        PdfViewResolver resolver = new PdfViewResolver();
        assertNotNull(resolver);
    }

    @Test
    public void testResolveViewName() throws Exception {
        PdfViewResolver resolver = new PdfViewResolver();
        View view = resolver.resolveViewName("test", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof PdfView);
    }

    @Test
    public void testResolveViewNameWithNullName() throws Exception {
        PdfViewResolver resolver = new PdfViewResolver();
        View view = resolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    public void testResolveViewNameWithNullLocale() throws Exception {
        PdfViewResolver resolver = new PdfViewResolver();
        View view = resolver.resolveViewName("test", null);
        assertNotNull(view);
    }
}
