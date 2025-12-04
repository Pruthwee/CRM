package crm.service;

import crm.entity.Pdf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PdfServiceTest {

    @Test
    public void testPdfServiceInterfaceExists() {
        assertNotNull(PdfService.class);
    }

    @Test
    public void testFindByNameMethodExists() throws Exception {
        assertNotNull(PdfService.class.getDeclaredMethod("findByName", String.class));
    }

    @Test
    public void testSavePdfMethodExists() throws Exception {
        assertNotNull(PdfService.class.getDeclaredMethod("savePdf", Pdf.class));
    }
}
