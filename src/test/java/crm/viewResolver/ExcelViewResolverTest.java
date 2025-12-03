package crm.viewResolver;

import crm.view.ExcelView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ExcelViewResolverTest {

    private ExcelViewResolver excelViewResolver;

    @BeforeEach
    void setUp() {
        excelViewResolver = new ExcelViewResolver();
    }

    @Test
    void testConstructor() {
        assertNotNull(excelViewResolver);
    }

    @Test
    void testResolveViewName() throws Exception {
        View view = excelViewResolver.resolveViewName("test", Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    void testResolveViewNameWithNullString() throws Exception {
        View view = excelViewResolver.resolveViewName(null, Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    void testResolveViewNameWithEmptyString() throws Exception {
        View view = excelViewResolver.resolveViewName("", Locale.getDefault());

        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    void testResolveViewNameWithNullLocale() throws Exception {
        View view = excelViewResolver.resolveViewName("test", null);

        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    void testResolveViewNameReturnsExcelView() throws Exception {
        View view = excelViewResolver.resolveViewName("anyName", Locale.US);

        assertTrue(view instanceof ExcelView);
    }
}
