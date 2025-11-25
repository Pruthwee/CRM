package crm.viewResolver;

import crm.view.ExcelView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelViewResolverTest {

    private ExcelViewResolver excelViewResolver;

    @BeforeEach
    public void setUp() {
        excelViewResolver = new ExcelViewResolver();
    }

    @Test
    public void testResolveViewName() throws Exception {
        View view = excelViewResolver.resolveViewName("testView", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    public void testResolveViewNameWithNullViewName() throws Exception {
        View view = excelViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    public void testResolveViewNameWithNullLocale() throws Exception {
        View view = excelViewResolver.resolveViewName("testView", null);
        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    public void testResolveViewNameReturnsExcelView() throws Exception {
        View view = excelViewResolver.resolveViewName("anyView", Locale.US);
        assertEquals(ExcelView.class, view.getClass());
    }

    @Test
    public void testResolveViewNameWithDifferentLocales() throws Exception {
        View view1 = excelViewResolver.resolveViewName("view1", Locale.FRENCH);
        View view2 = excelViewResolver.resolveViewName("view2", Locale.GERMAN);

        assertNotNull(view1);
        assertNotNull(view2);
        assertTrue(view1 instanceof ExcelView);
        assertTrue(view2 instanceof ExcelView);
    }

    @Test
    public void testViewResolverNotNull() {
        assertNotNull(excelViewResolver);
    }
}
