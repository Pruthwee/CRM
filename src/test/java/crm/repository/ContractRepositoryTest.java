package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContractRepositoryTest {

    @Test
    void testRepositoryInterface() {
        assertNotNull(ContractRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(ContractRepository.class));
    }

    @Test
    void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractRepository.class.getMethod("findByName", String.class));
    }

    @Test
    void testFindAllByValueLessThanEqualMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractRepository.class.getMethod("findAllByValueLessThanEqual", java.math.BigDecimal.class));
    }
}
