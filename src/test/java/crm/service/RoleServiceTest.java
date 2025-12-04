package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleServiceTest {

    @Test
    public void testRoleServiceInterfaceExists() {
        assertNotNull(RoleService.class);
    }

    @Test
    public void testListAllRolesMethodExists() throws Exception {
        assertNotNull(RoleService.class.getDeclaredMethod("listAllRoles"));
    }
}
