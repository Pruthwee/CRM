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
        pdf = new Pdf();
        pdf.setId(1L);
        pdf.setName("test.pdf");
        pdf.setContent("Test PDF content");
    }

    @Test
    void testConstructor() {
        PdfServiceImpl service = new PdfServiceImpl(pdfRepository);
        assertNotNull(service);
    }

    @Test
    void testFindByName_found() {
        when(pdfRepository.findByName("test.pdf")).thenReturn(pdf);
        Pdf result = pdfService.findByName("test.pdf");
        assertNotNull(result);
        assertEquals("test.pdf", result.getName());
        verify(pdfRepository).findByName("test.pdf");
    }

    @Test
    void testFindByName_notFound() {
        when(pdfRepository.findByName("unknown.pdf")).thenReturn(null);
        Pdf result = pdfService.findByName("unknown.pdf");
        assertNull(result);
        verify(pdfRepository).findByName("unknown.pdf");
    }

    @Test
    void testFindByName_emptyString() {
        when(pdfRepository.findByName("")).thenReturn(null);
        Pdf result = pdfService.findByName("");
        assertNull(result);
        verify(pdfRepository).findByName("");
    }

    @Test
    void testSavePdf_callsRepositorySave() {
        pdfService.savePdf(pdf);
        verify(pdfRepository, times(1)).save(pdf);
    }

    @Test
    void testSavePdf_newPdf() {
        Pdf newPdf = new Pdf();
        newPdf.setName("new.pdf");
        newPdf.setContent("New content");
        pdfService.savePdf(newPdf);
        verify(pdfRepository).save(newPdf);
    }

    @Test
    void testSavePdf_withNullContent() {
        Pdf pdfNoContent = new Pdf();
        pdfNoContent.setName("noContent.pdf");
        pdfNoContent.setContent(null);
        pdfService.savePdf(pdfNoContent);
        verify(pdfRepository).save(pdfNoContent);
    }
}
