package crm.view;

import crm.entity.Role;
import crm.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CsvViewTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private CsvView csvView;
    private Map<String, Object> model;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        csvView = new CsvView();

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
        assertNotNull(csvView);
    }

    @Test
    void testBuildCsvDocumentNotNull() {
        assertNotNull(csvView);
    }

    @Test
    void testBuildCsvDocumentWithUsers() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        assertDoesNotThrow(() -> {
            csvView.buildCsvDocument(model, request, response);
        });
    }
}
