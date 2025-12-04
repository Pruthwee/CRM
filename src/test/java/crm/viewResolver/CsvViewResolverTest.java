package crm.viewResolver;

import crm.view.CsvView;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class CsvViewResolverTest {

    @Test
    public void testCsvViewResolverConstructor() {
        CsvViewResolver resolver = new CsvViewResolver();
        assertNotNull(resolver);
    }

    @Test
    public void testResolveViewName() throws Exception {
        CsvViewResolver resolver = new CsvViewResolver();
        View view = resolver.resolveViewName("test", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    public void testResolveViewNameWithNullName() throws Exception {
        CsvViewResolver resolver = new CsvViewResolver();
        View view = resolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    public void testResolveViewNameWithNullLocale() throws Exception {
        CsvViewResolver resolver = new CsvViewResolver();
        View view = resolver.resolveViewName("test", null);
        assertNotNull(view);
    }
}
