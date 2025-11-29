package crm.service;

import crm.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Test
    void testUserServiceInterface() {
        assertNotNull(UserService.class);
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
        assertNotNull(UserService.class.getMethod("saveUser", User.class));
    }

    @Test
    void testEditUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("editUser", User.class));
    }

    @Test
    void testDeleteUserMethodExists() throws NoSuchMethodException {
        assertNotNull(UserService.class.getMethod("deleteUser", User.class));
    }

    @Test
    void testInterfaceHasCorrectMethodCount() {
        assertEquals(6, UserService.class.getDeclaredMethods().length);
    }

    @Test
    void testInterfaceIsPublic() {
        int modifiers = UserService.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
    }
}
