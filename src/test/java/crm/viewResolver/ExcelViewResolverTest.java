package crm.viewResolver;

import crm.view.ExcelView;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelViewResolverTest {

    @Test
    public void testExcelViewResolverConstructor() {
        ExcelViewResolver resolver = new ExcelViewResolver();
        assertNotNull(resolver);
    }

    @Test
    public void testResolveViewName() throws Exception {
        ExcelViewResolver resolver = new ExcelViewResolver();
        View view = resolver.resolveViewName("test", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(view instanceof ExcelView);
    }

    @Test
    public void testResolveViewNameWithNullName() throws Exception {
        ExcelViewResolver resolver = new ExcelViewResolver();
        View view = resolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    public void testResolveViewNameWithNullLocale() throws Exception {
        ExcelViewResolver resolver = new ExcelViewResolver();
        View view = resolver.resolveViewName("test", null);
        assertNotNull(view);
    }
}
