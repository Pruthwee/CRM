package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {

    @Test
    public void testUserRepositoryInterfaceExists() {
        assertNotNull(UserRepository.class);
    }

    @Test
    public void testFindByUsernameMethodExists() throws Exception {
        assertNotNull(UserRepository.class.getDeclaredMethod("findByUsername", String.class));
    }

    @Test
    public void testFindAllByEnabledMethodExists() throws Exception {
        assertNotNull(UserRepository.class.getDeclaredMethod("findAllByEnabled", int.class));
    }
}
