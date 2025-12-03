package crm.controller;

import crm.entity.User;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private RegisterController registerController;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void testConstructor() {
        UserService service = mock(UserService.class);
        RegisterController controller = new RegisterController(service);
        assertNotNull(controller);
    }

    @Test
    void testShowRegistrationPage() {
        String result = registerController.showRegistrationPage(model, testUser);

        assertEquals("register", result);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void testShowRegistrationPageWithNullUser() {
        String result = registerController.showRegistrationPage(model, null);

        assertEquals("register", result);
        verify(model).addAttribute("user", null);
    }

    @Test
    void testProcessRegistrationFormSuccess() {
        when(userService.findByUsername("testuser")).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = registerController.processRegistrationForm(model, testUser, bindingResult);

        assertEquals("success", result);
        verify(userService).saveUser(testUser);
    }

    @Test
    void testProcessRegistrationFormUserAlreadyExists() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        String result = registerController.processRegistrationForm(model, testUser, bindingResult);

        assertEquals("register", result);
        verify(model).addAttribute(eq("alreadyRegisteredMessage"), anyString());
        verify(bindingResult).reject("email");
    }

    @Test
    void testProcessRegistrationFormValidationErrors() {
        when(userService.findByUsername("testuser")).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = registerController.processRegistrationForm(model, testUser, bindingResult);

        assertEquals("redirect:/register", result);
        verify(userService, never()).saveUser(any());
    }

    @Test
    void testProcessRegistrationFormNullUser() {
        when(userService.findByUsername(null)).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(false);

        User nullUser = User.builder().build();
        String result = registerController.processRegistrationForm(model, nullUser, bindingResult);

        assertEquals("success", result);
    }
}
