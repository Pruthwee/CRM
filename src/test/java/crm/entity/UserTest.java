package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        user = new User();
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");
    }

    @Test
    @DisplayName("Should create user with default constructor")
    void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
    }

    @Test
    @DisplayName("Should create user with builder")
    void testBuilderConstructor() {
        User builtUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .enabled(1)
                .role(role)
                .build();

        assertNotNull(builtUser);
        assertEquals(1L, builtUser.getId());
        assertEquals("johndoe", builtUser.getUsername());
        assertEquals("john@example.com", builtUser.getEmail());
        assertEquals("John", builtUser.getFirstName());
        assertEquals("Doe", builtUser.getLastName());
        assertEquals("password123", builtUser.getPassword());
        assertEquals(1, builtUser.getEnabled());
        assertEquals(role, builtUser.getRole());
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testSettersAndGetters() {
        user.setId(2L);
        user.setUsername("janedoe");
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setPassword("pass456");
        user.setEnabled(1);
        user.setRole(role);

        assertEquals(2L, user.getId());
        assertEquals("janedoe", user.getUsername());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("pass456", user.getPassword());
        assertEquals(1, user.getEnabled());
        assertEquals(role, user.getRole());
    }

    @Test
    @DisplayName("Should get full name correctly")
    void testGetName() {
        user.setFirstName("John");
        user.setLastName("Doe");
        assertEquals("John Doe", user.getName());
    }

    @Test
    @DisplayName("Should get role id correctly")
    void testGetRoleId() {
        user.setRole(role);
        assertEquals(1, user.getRole_id());
    }

    @Test
    @DisplayName("Should get role name correctly")
    void testGetRoleName() {
        user.setRole(role);
        assertEquals("ROLE_USER", user.getRole_name());
    }

    @Test
    @DisplayName("Should get column count")
    void testGetColumnCount() {
        int columnCount = user.getColumnCount();
        assertTrue(columnCount > 0);
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setPassword(null);
        user.setRole(null);

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getPassword());
        assertNull(user.getRole());
    }

    @Test
    @DisplayName("Should handle null first name in getName")
    void testGetNameWithNullFirstName() {
        user.setFirstName(null);
        user.setLastName("Doe");
        assertEquals("null Doe", user.getName());
    }

    @Test
    @DisplayName("Should handle null last name in getName")
    void testGetNameWithNullLastName() {
        user.setFirstName("John");
        user.setLastName(null);
        assertEquals("John null", user.getName());
    }

    @Test
    @DisplayName("Should handle null role in getRole_id")
    void testGetRoleIdWithNullRole() {
        user.setRole(null);
        assertThrows(NullPointerException.class, () -> user.getRole_id());
    }

    @Test
    @DisplayName("Should handle null role in getRole_name")
    void testGetRoleNameWithNullRole() {
        user.setRole(null);
        assertThrows(NullPointerException.class, () -> user.getRole_name());
    }

    @Test
    @DisplayName("Should handle enabled flag")
    void testEnabledFlag() {
        user.setEnabled(1);
        assertEquals(1, user.getEnabled());

        user.setEnabled(0);
        assertEquals(0, user.getEnabled());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        user.setUsername("");
        user.setEmail("");
        user.setFirstName("");
        user.setLastName("");
        user.setPassword("");

        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
        assertEquals("", user.getPassword());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        User user1 = User.builder()
                .id(1L)
                .username("same")
                .email("same@example.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("same")
                .email("same@example.com")
                .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        user.setId(1L);
        user.setUsername("testuser");
        String result = user.toString();
        assertNotNull(result);
        assertTrue(result.contains("User"));
    }

    @Test
    @DisplayName("Should handle all args constructor")
    void testAllArgsConstructor() {
        User newUser = new User(
                3L,
                "newuser",
                "new@example.com",
                "Alice",
                "Johnson",
                "password789",
                1,
                role
        );

        assertEquals(3L, newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("new@example.com", newUser.getEmail());
        assertEquals("Alice", newUser.getFirstName());
        assertEquals("Johnson", newUser.getLastName());
        assertEquals("password789", newUser.getPassword());
        assertEquals(1, newUser.getEnabled());
        assertEquals(role, newUser.getRole());
    }

    @Test
    @DisplayName("Should handle no args constructor")
    void testNoArgsConstructor() {
        User newUser = new User();
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
    }

    @Test
    @DisplayName("Should handle special characters in fields")
    void testSpecialCharacters() {
        user.setUsername("user_123");
        user.setFirstName("José");
        user.setLastName("O'Brien");

        assertEquals("user_123", user.getUsername());
        assertEquals("José", user.getFirstName());
        assertEquals("O'Brien", user.getLastName());
    }

    @Test
    @DisplayName("Should handle long password")
    void testLongPassword() {
        String longPassword = "p".repeat(1000);
        user.setPassword(longPassword);
        assertEquals(longPassword, user.getPassword());
    }

    @Test
    @DisplayName("Should test getName with empty strings")
    void testGetNameWithEmptyStrings() {
        user.setFirstName("");
        user.setLastName("");
        assertEquals(" ", user.getName());
    }
}
