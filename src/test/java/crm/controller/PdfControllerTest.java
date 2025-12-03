package crm.controller;

import crm.entity.Pdf;
import crm.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfControllerTest {

    @Mock
    private PdfService pdfService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private PdfController pdfController;

    private Pdf testPdf;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testPdf = Pdf.builder()
                .name("test.pdf")
                .content("Test content")
                .build();
    }

    @Test
    void testConstructor() {
        PdfService service = mock(PdfService.class);
        PdfController controller = new PdfController(service);
        assertNotNull(controller);
    }

    @Test
    void testPdfGenerator() {
        String result = pdfController.pdfGenerator(model);

        assertEquals("pdf/generator", result);
        verify(model).addAttribute(eq("pdf"), any(Pdf.class));
    }

    @Test
    void testGeneratePdfSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = pdfController.generatePdf(testPdf, bindingResult);

        assertEquals("pdf/success", result);
        verify(pdfService).savePdf(testPdf);
    }

    @Test
    void testGeneratePdfValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = pdfController.generatePdf(testPdf, bindingResult);

        assertEquals("redirect:/pdf-generator", result);
        verify(pdfService, never()).savePdf(any());
    }

    @Test
    void testGeneratePdfWithoutExtension() {
        testPdf.setName("test");
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = pdfController.generatePdf(testPdf, bindingResult);

        assertEquals("pdf/success", result);
    }

    @Test
    void testGeneratePdfWithExtension() {
        testPdf.setName("test.pdf");
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = pdfController.generatePdf(testPdf, bindingResult);

        assertEquals("pdf/success", result);
    }
}
