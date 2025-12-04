package crm.viewResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvViewResolver Tests")
class CsvViewResolverTest {

    private CsvViewResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CsvViewResolver();
    }

    @Test
    @DisplayName("Should create CsvViewResolver instance")
    void testInstantiation() {
        assertNotNull(resolver);
    }

    @Test
    @DisplayName("Should resolve view for csv")
    void testResolveView() throws Exception {
        View view = resolver.resolveViewName("test", Locale.getDefault());
        assertNotNull(view);
    }
}
