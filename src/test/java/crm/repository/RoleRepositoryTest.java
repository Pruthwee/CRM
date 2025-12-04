package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleRepositoryTest {

    @Test
    public void testRoleRepositoryInterfaceExists() {
        assertNotNull(RoleRepository.class);
    }

    @Test
    public void testFindByNameMethodExists() throws Exception {
        assertNotNull(RoleRepository.class.getDeclaredMethod("findByName", String.class));
    }
}
