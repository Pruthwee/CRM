package crm.service;

import crm.entity.Pdf;
import crm.repository.PdfRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfServiceImplTest {

    @Mock
    private PdfRepository pdfRepository;

    @InjectMocks
    private PdfServiceImpl pdfService;

    private Pdf testPdf;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testPdf = Pdf.builder()
                .id(1L)
                .name("test.pdf")
                .content("Test content")
                .build();
    }

    @Test
    void testConstructor() {
        PdfRepository repo = mock(PdfRepository.class);
        PdfServiceImpl service = new PdfServiceImpl(repo);
        assertNotNull(service);
    }

    @Test
    void testConstructorWithNull() {
        assertDoesNotThrow(() -> new PdfServiceImpl(null));
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
    void testFindByNameNull() {
        when(pdfRepository.findByName(null)).thenReturn(null);

        Pdf result = pdfService.findByName(null);

        assertNull(result);
        verify(pdfRepository).findByName(null);
    }

    @Test
    void testFindByNameEmpty() {
        when(pdfRepository.findByName("")).thenReturn(null);

        Pdf result = pdfService.findByName("");

        assertNull(result);
        verify(pdfRepository).findByName("");
    }

    @Test
    void testSavePdf() {
        when(pdfRepository.save(testPdf)).thenReturn(testPdf);

        pdfService.savePdf(testPdf);

        verify(pdfRepository).save(testPdf);
    }

    @Test
    void testSavePdfNull() {
        assertDoesNotThrow(() -> pdfService.savePdf(null));
        verify(pdfRepository).save(null);
    }

    @Test
    void testSavePdfWithoutId() {
        Pdf newPdf = Pdf.builder()
                .name("new.pdf")
                .content("New content")
                .build();

        when(pdfRepository.save(newPdf)).thenReturn(newPdf);

        pdfService.savePdf(newPdf);

        verify(pdfRepository).save(newPdf);
    }

    @Test
    void testSavePdfUpdatesExisting() {
        testPdf.setContent("Updated content");
        when(pdfRepository.save(testPdf)).thenReturn(testPdf);

        pdfService.savePdf(testPdf);

        verify(pdfRepository).save(testPdf);
    }
}
