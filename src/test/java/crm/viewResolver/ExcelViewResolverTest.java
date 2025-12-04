package crm.viewResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExcelViewResolver Tests")
class ExcelViewResolverTest {

    private ExcelViewResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExcelViewResolver();
    }

    @Test
    @DisplayName("Should create ExcelViewResolver instance")
    void testInstantiation() {
        assertNotNull(resolver);
    }

    @Test
    @DisplayName("Should resolve view for excel")
    void testResolveView() throws Exception {
        View view = resolver.resolveViewName("test", Locale.getDefault());
        assertNotNull(view);
    }
}
