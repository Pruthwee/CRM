package crm.controller;

import crm.entity.User;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private RegisterController registerController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("newuser");
        user.setEmail("new@test.com");
        user.setFirstName("New");
        user.setLastName("User");
        user.setPassword("password");
        user.setEnabled(1);
    }

    @Test
    void testConstructor() {
        RegisterController controller = new RegisterController(userService);
        assertNotNull(controller);
    }

    @Test
    void testShowRegistrationPage_returnsRegisterView() {
        String view = registerController.showRegistrationPage(model, user);
        assertEquals("register", view);
        verify(model).addAttribute(eq("user"), eq(user));
    }

    @Test
    void testProcessRegistrationForm_userAlreadyExists_returnsRegisterView() {
        when(userService.findByUsername("newuser")).thenReturn(user);
        String view = registerController.processRegistrationForm(model, user, bindingResult);
        assertEquals("register", view);
        verify(model).addAttribute(eq("alreadyRegisteredMessage"), anyString());
        verify(bindingResult).reject("email");
        verify(userService, never()).saveUser(any());
    }

    @Test
    void testProcessRegistrationForm_withBindingErrors_redirectsToRegister() {
        when(userService.findByUsername("newuser")).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = registerController.processRegistrationForm(model, user, bindingResult);
        assertEquals("redirect:/register", view);
        verify(userService, never()).saveUser(any());
    }

    @Test
    void testProcessRegistrationForm_success_savesUserAndReturnsSuccess() {
        when(userService.findByUsername("newuser")).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = registerController.processRegistrationForm(model, user, bindingResult);
        assertEquals("success", view);
        verify(userService).saveUser(user);
    }

    @Test
    void testProcessRegistrationForm_newUser_noErrors() {
        User newUser = new User();
        newUser.setUsername("brandnewuser");
        newUser.setEmail("brandnew@test.com");
        newUser.setPassword("pass");

        when(userService.findByUsername("brandnewuser")).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = registerController.processRegistrationForm(model, newUser, bindingResult);
        assertEquals("success", view);
        verify(userService).saveUser(newUser);
    }

    @Test
    void testShowRegistrationPage_addsUserToModel() {
        registerController.showRegistrationPage(model, user);
        verify(model).addAttribute("user", user);
    }

    @Test
    void testProcessRegistrationForm_existingUser_doesNotSave() {
        when(userService.findByUsername("newuser")).thenReturn(user);
        registerController.processRegistrationForm(model, user, bindingResult);
        verify(userService, never()).saveUser(any());
    }
}
