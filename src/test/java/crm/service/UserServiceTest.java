package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(UserService.class);
    }

    @Test
    void testIsInterface() {
        assertTrue(UserService.class.isInterface());
    }

    @Test
    void testFindByUsernameMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("findByUsername", String.class));
    }

    @Test
    void testListAllUsersMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("listAllUsers"));
    }

    @Test
    void testShowUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("showUser", Long.class));
    }

    @Test
    void testSaveUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("saveUser", crm.entity.User.class));
    }

    @Test
    void testEditUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("editUser", crm.entity.User.class));
    }

    @Test
    void testDeleteUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("deleteUser", crm.entity.User.class));
    }
}
