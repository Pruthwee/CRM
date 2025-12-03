package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .enabled(1)
                .role(role)
                .build();
    }

    @Test
    void testConstructor() {
        assertNotNull(user);
    }

    @Test
    void testBuilder() {
        User built = User.builder()
                .username("builder")
                .email("builder@test.com")
                .build();

        assertNotNull(built);
        assertEquals("builder", built.getUsername());
    }

    @Test
    void testId() {
        assertEquals(1L, user.getId());
        user.setId(2L);
        assertEquals(2L, user.getId());
    }

    @Test
    void testUsername() {
        assertEquals("testuser", user.getUsername());
        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());
    }

    @Test
    void testEmail() {
        assertEquals("test@example.com", user.getEmail());
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void testFirstName() {
        assertEquals("John", user.getFirstName());
        user.setFirstName("Jane");
        assertEquals("Jane", user.getFirstName());
    }

    @Test
    void testLastName() {
        assertEquals("Doe", user.getLastName());
        user.setLastName("Smith");
        assertEquals("Smith", user.getLastName());
    }

    @Test
    void testPassword() {
        assertEquals("password123", user.getPassword());
        user.setPassword("newpass");
        assertEquals("newpass", user.getPassword());
    }

    @Test
    void testEnabled() {
        assertEquals(1, user.getEnabled());
        user.setEnabled(0);
        assertEquals(0, user.getEnabled());
    }

    @Test
    void testRole() {
        assertNotNull(user.getRole());
        assertEquals("ROLE_USER", user.getRole().getName());
    }

    @Test
    void testGetColumnCount() {
        int columnCount = user.getColumnCount();
        assertTrue(columnCount > 0);
    }

    @Test
    void testGetRoleId() {
        assertEquals(1, user.getRole_id());
    }

    @Test
    void testGetRoleName() {
        assertEquals("ROLE_USER", user.getRole_name());
    }

    @Test
    void testGetName() {
        assertEquals("John Doe", user.getName());
    }

    @Test
    void testGetNameWithNullFirstName() {
        user.setFirstName(null);
        assertEquals("null Doe", user.getName());
    }

    @Test
    void testGetNameWithNullLastName() {
        user.setLastName(null);
        assertEquals("John null", user.getName());
    }

    @Test
    void testNoArgsConstructor() {
        User empty = new User();
        assertNotNull(empty);
    }

    @Test
    void testAllArgsConstructor() {
        User full = new User(1L, "user", "email@test.com", "First", "Last", "pass", 1, role);
        assertNotNull(full);
        assertEquals("user", full.getUsername());
    }
}
