package crm.service;

import crm.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void testUserServiceInterfaceExists() {
        assertNotNull(UserService.class);
    }

    @Test
    public void testFindByUsernameMethodExists() throws Exception {
        assertNotNull(UserService.class.getDeclaredMethod("findByUsername", String.class));
    }

    @Test
    public void testListAllUsersMethodExists() throws Exception {
        assertNotNull(UserService.class.getDeclaredMethod("listAllUsers"));
    }

    @Test
    public void testShowUserMethodExists() throws Exception {
        assertNotNull(UserService.class.getDeclaredMethod("showUser", Long.class));
    }

    @Test
    public void testSaveUserMethodExists() throws Exception {
        assertNotNull(UserService.class.getDeclaredMethod("saveUser", User.class));
    }

    @Test
    public void testEditUserMethodExists() throws Exception {
        assertNotNull(UserService.class.getDeclaredMethod("editUser", User.class));
    }

    @Test
    public void testDeleteUserMethodExists() throws Exception {
        assertNotNull(UserService.class.getDeclaredMethod("deleteUser", User.class));
    }
}
