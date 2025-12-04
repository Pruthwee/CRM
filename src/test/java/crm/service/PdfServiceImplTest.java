package crm.service;

import crm.entity.Pdf;
import crm.repository.PdfRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfServiceImpl Tests")
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
                .name("Test Document")
                .content("PDF content")
                .build();
    }

    @Test
    @DisplayName("Should find pdf by name")
    void testFindByName() {
        when(pdfRepository.findByName("Test Document")).thenReturn(pdf);

        Pdf result = pdfService.findByName("Test Document");

        assertNotNull(result);
        assertEquals("Test Document", result.getName());
        verify(pdfRepository, times(1)).findByName("Test Document");
    }

    @Test
    @DisplayName("Should return null when pdf not found")
    void testFindByNameNotFound() {
        when(pdfRepository.findByName("NonExistent")).thenReturn(null);

        Pdf result = pdfService.findByName("NonExistent");

        assertNull(result);
        verify(pdfRepository, times(1)).findByName("NonExistent");
    }

    @Test
    @DisplayName("Should save pdf")
    void testSavePdf() {
        when(pdfRepository.save(any(Pdf.class))).thenReturn(pdf);

        pdfService.savePdf(pdf);

        verify(pdfRepository, times(1)).save(pdf);
    }

    @Test
    @DisplayName("Should handle null in findByName")
    void testFindByNameNull() {
        when(pdfRepository.findByName(null)).thenReturn(null);

        Pdf result = pdfService.findByName(null);

        assertNull(result);
        verify(pdfRepository, times(1)).findByName(null);
    }

    @Test
    @DisplayName("Should handle empty name")
    void testFindByNameEmpty() {
        when(pdfRepository.findByName("")).thenReturn(null);

        Pdf result = pdfService.findByName("");

        assertNull(result);
        verify(pdfRepository, times(1)).findByName("");
    }

    @Test
    @DisplayName("Should save pdf with null content")
    void testSavePdfNullContent() {
        Pdf nullContentPdf = Pdf.builder().id(2L).name("Doc").content(null).build();
        when(pdfRepository.save(nullContentPdf)).thenReturn(nullContentPdf);

        pdfService.savePdf(nullContentPdf);

        verify(pdfRepository, times(1)).save(nullContentPdf);
    }
}
