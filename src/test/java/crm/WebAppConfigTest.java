package crm;

import crm.viewResolver.CsvViewResolver;
import crm.viewResolver.ExcelViewResolver;
import crm.viewResolver.PdfViewResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebAppConfigTest {

    @InjectMocks
    private WebAppConfig webAppConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webAppConfig = new WebAppConfig();
    }

    @Test
    void testAddViewControllers() {
        ViewControllerRegistry registry = mock(ViewControllerRegistry.class);

        webAppConfig.addViewControllers(registry);

        verify(registry, times(11)).addViewController(anyString());
        verify(registry).setOrder(anyInt());
    }

    @Test
    void testAddViewControllersLoginPath() {
        ViewControllerRegistry registry = mock(ViewControllerRegistry.class);

        webAppConfig.addViewControllers(registry);

        verify(registry).addViewController("/login");
    }

    @Test
    void testConfigureContentNegotiation() {
        ContentNegotiationConfigurer configurer = mock(ContentNegotiationConfigurer.class);

        webAppConfig.configureContentNegotiation(configurer);

        verify(configurer).ignoreAcceptHeader(false);
        verify(configurer).defaultContentType(MediaType.APPLICATION_JSON);
        verify(configurer).mediaTypes(anyMap());
    }

    @Test
    void testConfigureContentNegotiationMediaTypes() {
        ContentNegotiationConfigurer configurer = mock(ContentNegotiationConfigurer.class);

        webAppConfig.configureContentNegotiation(configurer);

        verify(configurer).mediaTypes(argThat(map ->
            map.containsKey("html") &&
            map.containsKey("json") &&
            map.containsKey("xls") &&
            map.containsKey("pdf") &&
            map.containsKey("csv")
        ));
    }

    @Test
    void testContentNegotiatingViewResolver() {
        ContentNegotiationManager manager = mock(ContentNegotiationManager.class);

        ViewResolver resolver = webAppConfig.contentNegotiatingViewResolver(manager);

        assertNotNull(resolver);
        assertTrue(resolver instanceof ContentNegotiatingViewResolver);
    }

    @Test
    void testContentNegotiatingViewResolverWithNullManager() {
        assertThrows(NullPointerException.class, () -> {
            webAppConfig.contentNegotiatingViewResolver(null);
        });
    }

    @Test
    void testTemplateResolver() {
        ClassLoaderTemplateResolver resolver = webAppConfig.templateResolver();

        assertNotNull(resolver);
        assertEquals("templates/", resolver.getPrefix());
        assertEquals(".html", resolver.getSuffix());
        assertEquals("HTML", resolver.getTemplateMode());
        assertEquals("UTF-8", resolver.getCharacterEncoding());
        assertFalse(resolver.isCacheable());
    }

    @Test
    void testTemplateResolverPrefix() {
        ClassLoaderTemplateResolver resolver = webAppConfig.templateResolver();

        assertEquals("templates/", resolver.getPrefix());
    }

    @Test
    void testTemplateResolverSuffix() {
        ClassLoaderTemplateResolver resolver = webAppConfig.templateResolver();

        assertEquals(".html", resolver.getSuffix());
    }

    @Test
    void testTemplateEngine() {
        SpringTemplateEngine engine = webAppConfig.templateEngine();

        assertNotNull(engine);
        assertNotNull(engine.getTemplateResolvers());
    }

    @Test
    void testTemplateEngineDialects() {
        SpringTemplateEngine engine = webAppConfig.templateEngine();

        assertNotNull(engine.getDialects());
        assertTrue(engine.getDialects().size() > 0);
    }

    @Test
    void testViewResolver() {
        ViewResolver resolver = webAppConfig.viewResolver();

        assertNotNull(resolver);
        assertTrue(resolver instanceof ThymeleafViewResolver);
    }

    @Test
    void testViewResolverEncoding() {
        ViewResolver resolver = webAppConfig.viewResolver();

        ThymeleafViewResolver thymeleafResolver = (ThymeleafViewResolver) resolver;
        assertEquals("UTF-8", thymeleafResolver.getCharacterEncoding());
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
    void testAllViewResolversCreated() {
        assertNotNull(webAppConfig.excelViewResolver());
        assertNotNull(webAppConfig.csvViewResolver());
        assertNotNull(webAppConfig.pdfViewResolver());
        assertNotNull(webAppConfig.viewResolver());
    }
}
