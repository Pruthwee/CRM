package crm.service;

import crm.entity.Pdf;
import crm.repository.PdfRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceImplTest {

    @Mock
    private PdfRepository pdfRepository;

    @InjectMocks
    private PdfServiceImpl pdfService;

    private Pdf pdf;

    @BeforeEach
    void setUp() {
        pdf = Pdf.builder()
                .id(1L)
                .name("document.pdf")
                .content("PDF content")
                .build();
    }

    @Test
    void testFindByName_Found() {
        when(pdfRepository.findByName("document.pdf")).thenReturn(pdf);
        Pdf result = pdfService.findByName("document.pdf");
        assertNotNull(result);
        assertEquals("document.pdf", result.getName());
        verify(pdfRepository).findByName("document.pdf");
    }

    @Test
    void testFindByName_NotFound() {
        when(pdfRepository.findByName("unknown.pdf")).thenReturn(null);
        Pdf result = pdfService.findByName("unknown.pdf");
        assertNull(result);
        verify(pdfRepository).findByName("unknown.pdf");
    }

    @Test
    void testSavePdf() {
        pdfService.savePdf(pdf);
        verify(pdfRepository).save(pdf);
    }

    @Test
    void testSavePdf_NewPdf() {
        Pdf newPdf = Pdf.builder()
                .name("new.pdf")
                .content("New content")
                .build();
        pdfService.savePdf(newPdf);
        verify(pdfRepository).save(newPdf);
    }

    @Test
    void testConstructorWithRepository() {
        PdfServiceImpl service = new PdfServiceImpl(pdfRepository);
        assertNotNull(service);
    }

    @Test
    void testFindByName_ReturnsCorrectContent() {
        when(pdfRepository.findByName("document.pdf")).thenReturn(pdf);
        Pdf result = pdfService.findByName("document.pdf");
        assertEquals("PDF content", result.getContent());
    }
}
