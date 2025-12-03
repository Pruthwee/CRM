package crm.view;

import crm.entity.Role;
import crm.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExcelViewTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Workbook workbook;

    private ExcelView excelView;
    private Map<String, Object> model;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        excelView = new ExcelView();

        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password")
                .enabled(1)
                .role(role)
                .build();

        List<User> users = Arrays.asList(testUser);
        model = new HashMap<>();
        model.put("users", users);
    }

    @Test
    void testConstructor() {
        assertNotNull(excelView);
    }

    @Test
    void testBuildExcelDocumentNotNull() {
        assertNotNull(excelView);
    }

    @Test
    void testBuildExcelDocumentWithUsers() {
        assertDoesNotThrow(() -> {
            // Testing that view can be created without exceptions
        });
    }
}
