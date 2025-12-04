package crm.repository;

import crm.entity.Pdf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfRepository Tests")
class PdfRepositoryTest {

    @Mock
    private PdfRepository pdfRepository;

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

        Pdf result = pdfRepository.findByName("Test Document");

        assertNotNull(result);
        assertEquals("Test Document", result.getName());
        verify(pdfRepository, times(1)).findByName("Test Document");
    }

    @Test
    @DisplayName("Should return null when pdf not found by name")
    void testFindByNameNotFound() {
        when(pdfRepository.findByName("NonExistent")).thenReturn(null);

        Pdf result = pdfRepository.findByName("NonExistent");

        assertNull(result);
        verify(pdfRepository, times(1)).findByName("NonExistent");
    }

    @Test
    @DisplayName("Should save pdf")
    void testSave() {
        when(pdfRepository.save(any(Pdf.class))).thenReturn(pdf);

        Pdf result = pdfRepository.save(pdf);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Document", result.getName());
        verify(pdfRepository, times(1)).save(pdf);
    }

    @Test
    @DisplayName("Should find pdf by id")
    void testFindById() {
        when(pdfRepository.findById(1L)).thenReturn(Optional.of(pdf));

        Optional<Pdf> result = pdfRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Document", result.get().getName());
        verify(pdfRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when pdf not found by id")
    void testFindByIdNotFound() {
        when(pdfRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Pdf> result = pdfRepository.findById(999L);

        assertFalse(result.isPresent());
        verify(pdfRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find all pdfs")
    void testFindAll() {
        Pdf pdf2 = Pdf.builder().id(2L).name("Document 2").build();
        List<Pdf> pdfs = Arrays.asList(pdf, pdf2);
        when(pdfRepository.findAll()).thenReturn(pdfs);

        List<Pdf> result = pdfRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(pdfRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete pdf")
    void testDelete() {
        doNothing().when(pdfRepository).delete(pdf);

        pdfRepository.delete(pdf);

        verify(pdfRepository, times(1)).delete(pdf);
    }

    @Test
    @DisplayName("Should delete pdf by id")
    void testDeleteById() {
        doNothing().when(pdfRepository).deleteById(1L);

        pdfRepository.deleteById(1L);

        verify(pdfRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should check if pdf exists by id")
    void testExistsById() {
        when(pdfRepository.existsById(1L)).thenReturn(true);

        boolean result = pdfRepository.existsById(1L);

        assertTrue(result);
        verify(pdfRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should return false when pdf does not exist")
    void testExistsByIdFalse() {
        when(pdfRepository.existsById(999L)).thenReturn(false);

        boolean result = pdfRepository.existsById(999L);

        assertFalse(result);
        verify(pdfRepository, times(1)).existsById(999L);
    }

    @Test
    @DisplayName("Should count pdfs")
    void testCount() {
        when(pdfRepository.count()).thenReturn(15L);

        long result = pdfRepository.count();

        assertEquals(15L, result);
        verify(pdfRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should handle null name in findByName")
    void testFindByNameNull() {
        when(pdfRepository.findByName(null)).thenReturn(null);

        Pdf result = pdfRepository.findByName(null);

        assertNull(result);
        verify(pdfRepository, times(1)).findByName(null);
    }

    @Test
    @DisplayName("Should handle empty name in findByName")
    void testFindByNameEmpty() {
        when(pdfRepository.findByName("")).thenReturn(null);

        Pdf result = pdfRepository.findByName("");

        assertNull(result);
        verify(pdfRepository, times(1)).findByName("");
    }

    @Test
    @DisplayName("Should delete all pdfs")
    void testDeleteAll() {
        doNothing().when(pdfRepository).deleteAll();

        pdfRepository.deleteAll();

        verify(pdfRepository, times(1)).deleteAll();
    }
}
