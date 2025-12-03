package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleRepositoryTest {

    @Test
    void testRepositoryInterface() {
        assertNotNull(RoleRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(RoleRepository.class));
    }

    @Test
    void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(RoleRepository.class.getMethod("findByName", String.class));
    }
}
