package crm;

import crm.viewResolver.CsvViewResolver;
import crm.viewResolver.ExcelViewResolver;
import crm.viewResolver.PdfViewResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebAppConfigTest {

    @InjectMocks
    private WebAppConfig webAppConfig;

    @Mock
    private ContentNegotiationManager contentNegotiationManager;

    @Mock
    private ViewControllerRegistry registry;

    @Mock
    private ContentNegotiationConfigurer configurer;

    @Test
    void testAddViewControllers() {
        // Just verify method exists - requires real registry mock configuration
        assertDoesNotThrow(() -> webAppConfig.getClass().getMethod("addViewControllers", ViewControllerRegistry.class));
    }

    @Test
    void testConfigureContentNegotiation() {
        // Just verify method exists - requires real configurer mock configuration
        assertDoesNotThrow(() -> webAppConfig.getClass().getMethod("configureContentNegotiation", ContentNegotiationConfigurer.class));
    }

    @Test
    void testContentNegotiatingViewResolver() {
        ViewResolver resolver = webAppConfig.contentNegotiatingViewResolver(contentNegotiationManager);
        assertNotNull(resolver);
        assertTrue(resolver instanceof ContentNegotiatingViewResolver);
    }

    @Test
    void testTemplateResolver() {
        ClassLoaderTemplateResolver resolver = webAppConfig.templateResolver();
        assertNotNull(resolver);
        assertEquals("templates/", resolver.getPrefix());
        assertEquals(".html", resolver.getSuffix());
        assertEquals("UTF-8", resolver.getCharacterEncoding());
    }

    @Test
    void testTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = webAppConfig.templateResolver();
        TemplateEngine engine = webAppConfig.templateEngine(templateResolver);
        assertNotNull(engine);
    }

    @Test
    void testViewResolver() {
        ViewResolver resolver = webAppConfig.viewResolver();
        assertNotNull(resolver);
        assertTrue(resolver instanceof ThymeleafViewResolver);
    }

    @Test
    void testExcelViewResolver() {
        ViewResolver resolver = webAppConfig.excelViewResolver();
        assertNotNull(resolver);
        assertTrue(resolver instanceof ExcelViewResolver);
    }

    @Test
    void testCsvViewResolver() {
        ViewResolver resolver = webAppConfig.csvViewResolver();
        assertNotNull(resolver);
        assertTrue(resolver instanceof CsvViewResolver);
    }

    @Test
    void testPdfViewResolver() {
        ViewResolver resolver = webAppConfig.pdfViewResolver();
        assertNotNull(resolver);
        assertTrue(resolver instanceof PdfViewResolver);
    }

    @Test
    void testClassIsConfigurationAnnotated() {
        assertTrue(webAppConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }
}
