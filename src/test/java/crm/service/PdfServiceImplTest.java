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

    private Pdf testPdf;

    @BeforeEach
    void setUp() {
        testPdf = Pdf.builder()
                .id(1L)
                .name("test.pdf")
                .content("Test content")
                .build();
    }

    @Test
    void testConstructor() {
        PdfServiceImpl service = new PdfServiceImpl(pdfRepository);
        assertNotNull(service);
    }

    @Test
    void testFindByName() {
        when(pdfRepository.findByName("test.pdf")).thenReturn(testPdf);

        Pdf result = pdfService.findByName("test.pdf");

        assertNotNull(result);
        assertEquals("test.pdf", result.getName());
        verify(pdfRepository).findByName("test.pdf");
    }

    @Test
    void testFindByNameNotFound() {
        when(pdfRepository.findByName("nonexistent.pdf")).thenReturn(null);

        Pdf result = pdfService.findByName("nonexistent.pdf");

        assertNull(result);
        verify(pdfRepository).findByName("nonexistent.pdf");
    }

    @Test
    void testSavePdf() {
        pdfService.savePdf(testPdf);

        verify(pdfRepository).save(testPdf);
    }

    @Test
    void testSavePdfWithNullContent() {
        testPdf.setContent(null);

        assertDoesNotThrow(() -> pdfService.savePdf(testPdf));
        verify(pdfRepository).save(testPdf);
    }
}
