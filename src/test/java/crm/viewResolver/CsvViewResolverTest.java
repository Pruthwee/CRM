package crm.viewResolver;

import crm.view.CsvView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CsvViewResolverTest {

    private CsvViewResolver csvViewResolver;

    @BeforeEach
    void setUp() {
        csvViewResolver = new CsvViewResolver();
    }

    @Test
    void testDefaultConstructor() {
        CsvViewResolver resolver = new CsvViewResolver();
        assertNotNull(resolver);
    }

    @Test
    void testResolveViewName_returnsNonNull() throws Exception {
        View view = csvViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_returnsCsvView() throws Exception {
        View view = csvViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertTrue(view instanceof CsvView);
    }

    @Test
    void testResolveViewName_withNullViewName() throws Exception {
        View view = csvViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_withDifferentLocale() throws Exception {
        View view = csvViewResolver.resolveViewName("test", Locale.FRENCH);
        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    void testResolveViewName_withEmptyViewName() throws Exception {
        View view = csvViewResolver.resolveViewName("", Locale.ENGLISH);
        assertNotNull(view);
    }
}
