package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
    }

    @Test
    public void testUserConstructor() {
        assertNotNull(user);
    }

    @Test
    public void testUserBuilderPattern() {
        User builtUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .enabled(1)
                .build();
        assertNotNull(builtUser);
        assertEquals("testuser", builtUser.getUsername());
        assertEquals("test@example.com", builtUser.getEmail());
    }

    @Test
    public void testAllArgsConstructor() {
        Role role = new Role();
        User fullUser = new User(1L, "testuser", "test@example.com", "Test", "User", "password", 1, role);
        assertNotNull(fullUser);
        assertEquals("testuser", fullUser.getUsername());
    }

    @Test
    public void testSetAndGetId() {
        user.setId(1L);
        assertEquals(1L, user.getId());
    }

    @Test
    public void testSetAndGetUsername() {
        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());
    }

    @Test
    public void testSetAndGetEmail() {
        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    public void testSetAndGetFirstName() {
        user.setFirstName("Test");
        assertEquals("Test", user.getFirstName());
    }

    @Test
    public void testSetAndGetLastName() {
        user.setLastName("User");
        assertEquals("User", user.getLastName());
    }

    @Test
    public void testSetAndGetPassword() {
        user.setPassword("password");
        assertEquals("password", user.getPassword());
    }

    @Test
    public void testSetAndGetEnabled() {
        user.setEnabled(1);
        assertEquals(1, user.getEnabled());
    }

    @Test
    public void testSetAndGetRole() {
        Role role = new Role();
        user.setRole(role);
        assertEquals(role, user.getRole());
    }

    @Test
    public void testGetColumnCount() {
        int count = user.getColumnCount();
        assertTrue(count > 0);
    }

    @Test
    public void testGetName() {
        user.setFirstName("Test");
        user.setLastName("User");
        assertEquals("Test User", user.getName());
    }

    @Test
    public void testGetRoleId() {
        Role role = new Role();
        role.setId(1);
        user.setRole(role);
        assertEquals(1, user.getRole_id());
    }

    @Test
    public void testGetRoleName() {
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRole(role);
        assertEquals("ROLE_USER", user.getRole_name());
    }
}
