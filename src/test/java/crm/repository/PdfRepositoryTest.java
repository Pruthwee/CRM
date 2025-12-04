package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PdfRepositoryTest {

    @Test
    public void testPdfRepositoryInterfaceExists() {
        assertNotNull(PdfRepository.class);
    }

    @Test
    public void testFindByNameMethodExists() throws Exception {
        assertNotNull(PdfRepository.class.getDeclaredMethod("findByName", String.class));
    }
}
