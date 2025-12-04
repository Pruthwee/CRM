package crm.controller;

import crm.entity.User;
import crm.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegisterControllerTest {

    private RegisterController registerController;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        registerController = new RegisterController(userService);
    }

    @Test
    public void testRegisterControllerConstructor() {
        assertNotNull(registerController);
    }

    @Test
    public void testShowRegistrationPage() {
        Model model = mock(Model.class);
        User user = new User();
        String viewName = registerController.showRegistrationPage(model, user);
        assertEquals("register", viewName);
        verify(model).addAttribute("user", user);
    }

    @Test
    public void testProcessRegistrationFormWithExistingUser() {
        Model model = mock(Model.class);
        User user = new User();
        user.setUsername("existingUser");
        BindingResult bindingResult = mock(BindingResult.class);

        when(userService.findByUsername("existingUser")).thenReturn(user);

        String viewName = registerController.processRegistrationForm(model, user, bindingResult);
        assertEquals("register", viewName);
        verify(model).addAttribute(eq("alreadyRegisteredMessage"), anyString());
    }

    @Test
    public void testProcessRegistrationFormWithValidationErrors() {
        Model model = mock(Model.class);
        User user = new User();
        user.setUsername("newUser");
        BindingResult bindingResult = mock(BindingResult.class);

        when(userService.findByUsername("newUser")).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = registerController.processRegistrationForm(model, user, bindingResult);
        assertEquals("redirect:/register", viewName);
    }

    @Test
    public void testProcessRegistrationFormSuccess() {
        Model model = mock(Model.class);
        User user = new User();
        user.setUsername("newUser");
        BindingResult bindingResult = mock(BindingResult.class);

        when(userService.findByUsername("newUser")).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = registerController.processRegistrationForm(model, user, bindingResult);
        assertEquals("success", viewName);
        verify(userService).saveUser(user);
    }
}
