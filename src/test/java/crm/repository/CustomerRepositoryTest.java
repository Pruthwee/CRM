package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRepositoryTest {

    @Test
    void testRepositoryInterface() {
        assertNotNull(CustomerRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(CustomerRepository.class));
    }

    @Test
    void testGetMaxIdMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerRepository.class.getMethod("getMaxId"));
    }

    @Test
    void testFindAllByEnabledMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerRepository.class.getMethod("findAllByEnabled", int.class));
    }

    @Test
    void testFindOneByEnabledAndNameMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerRepository.class.getMethod("findOneByEnabledAndName", int.class, String.class));
    }
}
