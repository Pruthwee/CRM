package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfServiceTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(PdfService.class);
    }

    @Test
    void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(PdfService.class.getMethod("findByName", String.class));
    }

    @Test
    void testSavePdfMethodExists() throws NoSuchMethodException {
        assertNotNull(PdfService.class.getMethod("savePdf", crm.entity.Pdf.class));
    }
}
