package crm.controller;

import crm.entity.Pdf;
import crm.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfControllerTest {

    @Mock
    private PdfService pdfService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private PdfController pdfController;

    private Pdf pdf;

    @BeforeEach
    void setUp() {
        pdf = Pdf.builder()
                .id(1L)
                .name("test")
                .content("Test PDF content")
                .build();
    }

    @Test
    void testPdfGenerator_Get() {
        String view = pdfController.pdfGenerator(model);
        assertEquals("pdf/generator", view);
        verify(model).addAttribute(eq("pdf"), any(Pdf.class));
    }

    @Test
    void testGeneratePdf_WithBindingErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = pdfController.generatePdf(pdf, bindingResult);
        assertEquals("redirect:/pdf-generator", view);
        verify(pdfService, never()).savePdf(any());
    }

    @Test
    void testGeneratePdf_Success_WithPdfExtension() {
        pdf.setName("test.pdf");
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = pdfController.generatePdf(pdf, bindingResult);
        assertEquals("pdf/success", view);
    }

    @Test
    void testGeneratePdf_Success_WithoutPdfExtension() {
        pdf.setName("test");
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = pdfController.generatePdf(pdf, bindingResult);
        assertEquals("pdf/success", view);
    }

    @Test
    void testConstructorWithService() {
        PdfController controller = new PdfController(pdfService);
        assertNotNull(controller);
    }
}
