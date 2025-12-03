package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryRepositoryTest {

    @Test
    void testRepositoryInterface() {
        assertNotNull(CategoryRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(CategoryRepository.class));
    }

    @Test
    void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(CategoryRepository.class.getMethod("findByName", String.class));
    }
}
