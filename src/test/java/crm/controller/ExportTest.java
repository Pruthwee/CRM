package crm.controller;

import crm.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExportTest {

    private Export export;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        export = new Export(userService);
    }

    @Test
    public void testExportConstructor() {
        assertNotNull(export);
    }

    @Test
    public void testDownload() {
        Model model = mock(Model.class);
        String viewName = export.download(model);
        assertEquals("", viewName);
        verify(model).addAttribute(eq("users"), any());
        verify(userService).listAllUsers();
    }
}
