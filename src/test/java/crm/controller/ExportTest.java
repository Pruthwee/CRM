package crm.controller;

import crm.entity.User;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private Export export;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    void testConstructor() {
        UserService service = mock(UserService.class);
        Export controller = new Export(service);
        assertNotNull(controller);
    }

    @Test
    void testDownload() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(testUser));

        String result = export.download(model);

        assertEquals("", result);
        verify(model).addAttribute("users", Arrays.asList(testUser));
        verify(userService).listAllUsers();
    }

    @Test
    void testDownloadEmptyList() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList());

        String result = export.download(model);

        assertEquals("", result);
        verify(model).addAttribute("users", Arrays.asList());
    }
}
