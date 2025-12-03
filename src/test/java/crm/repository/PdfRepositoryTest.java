package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfRepositoryTest {

    @Test
    void testRepositoryInterface() {
        assertNotNull(PdfRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(PdfRepository.class));
    }

    @Test
    void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(PdfRepository.class.getMethod("findByName", String.class));
    }
}
