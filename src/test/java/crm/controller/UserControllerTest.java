package crm.controller;

import crm.entity.Role;
import crm.entity.User;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private User user;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .enabled(1)
                .role(role)
                .build();
    }

    @Test
    void testShowAllUsers() {
        List<User> users = Collections.singletonList(user);
        when(currentUserDetails.getUsername()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(userService.listAllUsers()).thenReturn(users);

        String view = userController.showAllUsers(model, currentUserDetails);
        assertEquals("user/list", view);
        verify(model).addAttribute(eq("currentUser"), eq(user));
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testShowFormEditUser() {
        when(userService.showUser(1L)).thenReturn(user);
        String view = userController.showFormEditUser(model, 1L);
        assertEquals("user/edit", view);
        verify(model).addAttribute(eq("user"), eq(user));
    }

    @Test
    void testProcessRequestEditUser_NoErrors() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = userController.processRequestEditUser(1L, user, bindingResult);
        assertEquals("redirect:/user/list", view);
        verify(userService).editUser(user);
    }

    @Test
    void testProcessRequestEditUser_WithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = userController.processRequestEditUser(1L, user, bindingResult);
        assertEquals("redirect:/user/edit/1", view);
        verify(userService, never()).editUser(any());
    }

    @Test
    void testDeleteUser() {
        when(userService.showUser(1L)).thenReturn(user);
        String view = userController.deleteUser(1L);
        assertEquals("redirect:/user/list", view);
        verify(userService).deleteUser(user);
    }

    @Test
    void testDeleteUser_CallsShowUserFirst() {
        when(userService.showUser(2L)).thenReturn(user);
        userController.deleteUser(2L);
        verify(userService).showUser(2L);
        verify(userService).deleteUser(user);
    }

    @Test
    void testConstructorWithService() {
        UserController controller = new UserController(userService);
        assertNotNull(controller);
    }
}
