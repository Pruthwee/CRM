package crm.service;

import crm.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Test
    void testRoleServiceInterface() {
        assertNotNull(RoleService.class);
        assertTrue(RoleService.class.isInterface());
    }

    @Test
    void testListAllRolesMethodExists() throws NoSuchMethodException {
        assertNotNull(RoleService.class.getMethod("listAllRoles"));
    }

    @Test
    void testInterfaceHasCorrectMethodCount() {
        assertEquals(1, RoleService.class.getDeclaredMethods().length);
    }

    @Test
    void testInterfaceIsPublic() {
        int modifiers = RoleService.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
    }
}
