package crm.controller;

import crm.entity.Role;
import crm.entity.User;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private UserDetails currentUserDetails;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(role)
                .build();
    }

    @Test
    void testConstructor() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        assertNotNull(controller);
    }

    @Test
    void testShowAllUsers() {
        when(currentUserDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(userService.listAllUsers()).thenReturn(Arrays.asList(testUser));

        String result = userController.showAllUsers(model, currentUserDetails);

        assertEquals("user/list", result);
        verify(model).addAttribute("currentUser", testUser);
        verify(model).addAttribute("users", Arrays.asList(testUser));
    }

    @Test
    void testShowFormEditUser() {
        when(userService.showUser(1L)).thenReturn(testUser);

        String result = userController.showFormEditUser(model, 1L);

        assertEquals("user/edit", result);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void testShowFormEditUserNotFound() {
        when(userService.showUser(999L)).thenReturn(null);

        String result = userController.showFormEditUser(model, 999L);

        assertEquals("user/edit", result);
        verify(model).addAttribute("user", null);
    }

    @Test
    void testProcessRequestEditUserSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = userController.processRequestEditUser(1L, testUser, bindingResult);

        assertEquals("redirect:/user/list", result);
        verify(userService).editUser(testUser);
    }

    @Test
    void testProcessRequestEditUserValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = userController.processRequestEditUser(1L, testUser, bindingResult);

        assertEquals("redirect:/user/edit/1", result);
        verify(userService, never()).editUser(any());
    }

    @Test
    void testDeleteUser() {
        when(userService.showUser(1L)).thenReturn(testUser);

        String result = userController.deleteUser(1L);

        assertEquals("redirect:/user/list", result);
        verify(userService).deleteUser(testUser);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userService.showUser(999L)).thenReturn(null);

        String result = userController.deleteUser(999L);

        assertEquals("redirect:/user/list", result);
        verify(userService).deleteUser(null);
    }
}
