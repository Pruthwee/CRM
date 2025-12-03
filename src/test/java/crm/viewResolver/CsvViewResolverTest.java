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
    void testConstructor() {
        assertNotNull(csvViewResolver);
    }

    @Test
    void testResolveViewName() throws Exception {
        View view = csvViewResolver.resolveViewName("test", Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    void testResolveViewNameWithNullString() throws Exception {
        View view = csvViewResolver.resolveViewName(null, Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    void testResolveViewNameWithEmptyString() throws Exception {
        View view = csvViewResolver.resolveViewName("", Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    void testResolveViewNameWithNullLocale() throws Exception {
        View view = csvViewResolver.resolveViewName("test", null);

        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    void testResolveViewNameReturnsCsvView() throws Exception {
        View view = csvViewResolver.resolveViewName("anyName", Locale.US);

        assertTrue(view instanceof CsvView);
    }
}
