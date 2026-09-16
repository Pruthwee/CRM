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

        user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("secret");
        user.setEnabled(1);
        user.setRole(role);
    }

    @Test
    void testDefaultConstructor() {
        User u = new User();
        assertNotNull(u);
    }

    @Test
    void testAllArgsConstructor() {
        User u = new User(2L, "janedoe", "jane@example.com", "Jane", "Doe", "pass", 1, role);
        assertNotNull(u);
        assertEquals(2L, u.getId());
        assertEquals("janedoe", u.getUsername());
        assertEquals("jane@example.com", u.getEmail());
        assertEquals("Jane", u.getFirstName());
        assertEquals("Doe", u.getLastName());
        assertEquals("pass", u.getPassword());
        assertEquals(1, u.getEnabled());
    }

    @Test
    void testBuilder() {
        User u = User.builder()
                .id(3L)
                .username("builder_user")
                .email("builder@example.com")
                .firstName("Builder")
                .lastName("User")
                .password("buildpass")
                .enabled(1)
                .role(role)
                .build();
        assertNotNull(u);
        assertEquals(3L, u.getId());
        assertEquals("builder_user", u.getUsername());
    }

    @Test
    void testGetName() {
        assertEquals("John Doe", user.getName());
    }

    @Test
    void testGetNameWithNullFirstName() {
        user.setFirstName(null);
        // getName() returns null + " " + lastName
        String name = user.getName();
        assertNotNull(name);
        assertTrue(name.contains("Doe"));
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
    void testGetColumnCount() {
        int count = user.getColumnCount();
        assertTrue(count > 0);
    }

    @Test
    void testSetAndGetId() {
        user.setId(99L);
        assertEquals(99L, user.getId());
    }

    @Test
    void testSetAndGetUsername() {
        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());
    }

    @Test
    void testSetAndGetEmail() {
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void testSetAndGetFirstName() {
        user.setFirstName("Alice");
        assertEquals("Alice", user.getFirstName());
    }

    @Test
    void testSetAndGetLastName() {
        user.setLastName("Smith");
        assertEquals("Smith", user.getLastName());
    }

    @Test
    void testSetAndGetPassword() {
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());
    }

    @Test
    void testSetAndGetEnabled() {
        user.setEnabled(0);
        assertEquals(0, user.getEnabled());
    }

    @Test
    void testSetAndGetRole() {
        Role newRole = new Role();
        newRole.setId(2);
        newRole.setName("ROLE_ADMIN");
        user.setRole(newRole);
        assertEquals("ROLE_ADMIN", user.getRole().getName());
    }

    @Test
    void testEqualsAndHashCode() {
        User u1 = User.builder().id(1L).username("user1").email("u1@test.com").build();
        User u2 = User.builder().id(1L).username("user1").email("u1@test.com").build();
        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    void testNotEquals() {
        User u1 = User.builder().id(1L).username("user1").email("u1@test.com").build();
        User u2 = User.builder().id(2L).username("user2").email("u2@test.com").build();
        assertNotEquals(u1, u2);
    }

    @Test
    void testToString() {
        String str = user.toString();
        assertNotNull(str);
        assertTrue(str.contains("johndoe"));
    }
}
