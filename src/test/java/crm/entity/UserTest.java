package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private Role role;

    @BeforeEach
    public void setUp() {
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
    public void testUserBuilder() {
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("password123", user.getPassword());
        assertEquals(1, user.getEnabled());
        assertEquals(role, user.getRole());
    }

    @Test
    public void testUserNoArgsConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
    }

    @Test
    public void testUserAllArgsConstructor() {
        User newUser = new User(2L, "newuser", "new@example.com", "Jane", "Smith", "pass456", 1, role);
        assertNotNull(newUser);
        assertEquals(2L, newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("new@example.com", newUser.getEmail());
    }

    @Test
    public void testGetColumnCount() {
        int columnCount = user.getColumnCount();
        assertTrue(columnCount > 0);
    }

    @Test
    public void testGetRoleId() {
        assertEquals(1, user.getRole_id());
    }

    @Test
    public void testGetRoleName() {
        assertEquals("ROLE_USER", user.getRole_name());
    }

    @Test
    public void testGetName() {
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void testGetNameWithNullValues() {
        user.setFirstName(null);
        user.setLastName(null);
        assertEquals("null null", user.getName());
    }

    @Test
    public void testSettersAndGetters() {
        user.setUsername("updateduser");
        assertEquals("updateduser", user.getUsername());

        user.setEmail("updated@example.com");
        assertEquals("updated@example.com", user.getEmail());

        user.setFirstName("Jane");
        assertEquals("Jane", user.getFirstName());

        user.setLastName("Smith");
        assertEquals("Smith", user.getLastName());

        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());

        user.setEnabled(0);
        assertEquals(0, user.getEnabled());
    }

    @Test
    public void testUserWithNullRole() {
        user.setRole(null);
        assertNull(user.getRole());
        assertThrows(NullPointerException.class, () -> user.getRole_id());
        assertThrows(NullPointerException.class, () -> user.getRole_name());
    }

    @Test
    public void testUserEnabled() {
        user.setEnabled(1);
        assertEquals(1, user.getEnabled());

        user.setEnabled(0);
        assertEquals(0, user.getEnabled());
    }

    @Test
    public void testEmailValidation() {
        user.setEmail("valid@email.com");
        assertTrue(user.getEmail().contains("@"));
    }
}
