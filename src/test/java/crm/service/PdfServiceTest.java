package crm.service;

import crm.entity.Pdf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

    @Test
    void testPdfServiceInterface() {
        assertNotNull(PdfService.class);
        assertTrue(PdfService.class.isInterface());
    }

    @Test
    void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(PdfService.class.getMethod("findByName", String.class));
    }

    @Test
    void testSavePdfMethodExists() throws NoSuchMethodException {
        assertNotNull(PdfService.class.getMethod("savePdf", Pdf.class));
    }

    @Test
    void testInterfaceHasCorrectMethodCount() {
        assertEquals(2, PdfService.class.getDeclaredMethods().length);
    }

    @Test
    void testInterfaceIsPublic() {
        int modifiers = PdfService.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
    }
}
