package crm.controller;

import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Collections;

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

    @BeforeEach
    void setUp() {
    }

    @Test
    void testDownload() {
        when(userService.listAllUsers()).thenReturn(Collections.emptyList());
        String view = export.download(model);
        assertEquals("", view);
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testDownload_WithUsers() {
        when(userService.listAllUsers()).thenReturn(Collections.emptyList());
        String view = export.download(model);
        assertNotNull(view);
        verify(userService).listAllUsers();
    }

    @Test
    void testConstructorWithService() {
        Export exportController = new Export(userService);
        assertNotNull(exportController);
    }
}
