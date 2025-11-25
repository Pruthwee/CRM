package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleServiceTest {

    @Test
    public void testRoleServiceInterface() {
        assertNotNull(RoleService.class);
        assertTrue(RoleService.class.isInterface());
    }

    @Test
    public void testListAllRolesMethodExists() throws NoSuchMethodException {
        assertNotNull(RoleService.class.getMethod("listAllRoles"));
    }

    @Test
    public void testInterfaceHasOneMethod() {
        assertEquals(1, RoleService.class.getDeclaredMethods().length);
    }
}
