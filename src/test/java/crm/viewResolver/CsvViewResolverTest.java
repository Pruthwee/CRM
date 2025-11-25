package crm.viewResolver;

import crm.view.CsvView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class CsvViewResolverTest {

    private CsvViewResolver csvViewResolver;

    @BeforeEach
    public void setUp() {
        csvViewResolver = new CsvViewResolver();
    }

    @Test
    public void testResolveViewName() throws Exception {
        View view = csvViewResolver.resolveViewName("testView", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    public void testResolveViewNameWithNullViewName() throws Exception {
        View view = csvViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    public void testResolveViewNameWithNullLocale() throws Exception {
        View view = csvViewResolver.resolveViewName("testView", null);
        assertNotNull(view);
        assertTrue(view instanceof CsvView);
    }

    @Test
    public void testResolveViewNameReturnsCsvView() throws Exception {
        View view = csvViewResolver.resolveViewName("anyView", Locale.US);
        assertEquals(CsvView.class, view.getClass());
    }

    @Test
    public void testResolveViewNameWithDifferentLocales() throws Exception {
        View view1 = csvViewResolver.resolveViewName("view1", Locale.FRENCH);
        View view2 = csvViewResolver.resolveViewName("view2", Locale.GERMAN);

        assertNotNull(view1);
        assertNotNull(view2);
        assertTrue(view1 instanceof CsvView);
        assertTrue(view2 instanceof CsvView);
    }

    @Test
    public void testViewResolverNotNull() {
        assertNotNull(csvViewResolver);
    }
}
