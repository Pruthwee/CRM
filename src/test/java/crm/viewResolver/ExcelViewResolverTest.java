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
    void testDefaultConstructor() {
        ExcelViewResolver resolver = new ExcelViewResolver();
        assertNotNull(resolver);
    }

    @Test
    void testResolveViewName_returnsNonNull() throws Exception {
        View view = excelViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_returnsExcelView() throws Exception {
        View view = excelViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    void testResolveViewName_withNullViewName() throws Exception {
        View view = excelViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_withDifferentLocale() throws Exception {
        View view = excelViewResolver.resolveViewName("test", Locale.ITALIAN);
        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    void testResolveViewName_withEmptyViewName() throws Exception {
        View view = excelViewResolver.resolveViewName("", Locale.ENGLISH);
        assertNotNull(view);
    }
}
