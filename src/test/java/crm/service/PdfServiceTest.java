package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PdfServiceTest {

    @Test
    public void testPdfServiceInterface() {
        assertNotNull(PdfService.class);
        assertTrue(PdfService.class.isInterface());
    }

    @Test
    public void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(PdfService.class.getMethod("findByName", String.class));
    }

    @Test
    public void testSavePdfMethodExists() throws NoSuchMethodException {
        assertNotNull(PdfService.class.getMethod("savePdf", crm.entity.Pdf.class));
    }

    @Test
    public void testInterfaceHasTwoMethods() {
        assertEquals(2, PdfService.class.getDeclaredMethods().length);
    }
}
