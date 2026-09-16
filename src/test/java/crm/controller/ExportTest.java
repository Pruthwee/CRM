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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private Export export;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void testConstructor() {
        Export e = new Export(userService);
        assertNotNull(e);
    }

    @Test
    void testDownload_returnsEmptyString() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        String view = export.download(model);
        assertEquals("", view);
    }

    @Test
    void testDownload_addsUsersToModel() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        export.download(model);
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testDownload_callsListAllUsers() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        export.download(model);
        verify(userService).listAllUsers();
    }
}
