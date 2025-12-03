package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleServiceTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(RoleService.class);
    }

    @Test
    void testIsInterface() {
        assertTrue(RoleService.class.isInterface());
    }

    @Test
    void testListAllRolesMethodExists() throws NoSuchMethodException {
        assertNotNull(RoleService.class.getMethod("listAllRoles"));
    }
}
