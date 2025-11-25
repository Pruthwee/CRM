package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void testUserServiceInterface() {
        assertNotNull(UserService.class);
        assertTrue(UserService.class.isInterface());
    }

    @Test
    public void testFindByUsernameMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("findByUsername", String.class));
    }

    @Test
    public void testListAllUsersMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("listAllUsers"));
    }

    @Test
    public void testShowUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("showUser", Long.class));
    }

    @Test
    public void testSaveUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("saveUser", crm.entity.User.class));
    }

    @Test
    public void testEditUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("editUser", crm.entity.User.class));
    }

    @Test
    public void testDeleteUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("deleteUser", crm.entity.User.class));
    }

    @Test
    public void testInterfaceHasSixMethods() {
        assertEquals(6, UserService.class.getDeclaredMethods().length);
    }
}
