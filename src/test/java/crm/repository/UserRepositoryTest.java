package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    @Test
    void testRepositoryInterface() {
        assertNotNull(UserRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(UserRepository.class));
    }

    @Test
    void testFindByUsernameMethodExists() throws NoSuchMethodException {
        assertNotNull(UserRepository.class.getMethod("findByUsername", String.class));
    }

    @Test
    void testFindAllByEnabledMethodExists() throws NoSuchMethodException {
        assertNotNull(UserRepository.class.getMethod("findAllByEnabled", int.class));
    }
}
