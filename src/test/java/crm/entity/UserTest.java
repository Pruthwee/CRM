package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void testUserBuilder() {
        User built = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password")
                .enabled(1)
                .role(role)
                .build();

        assertNotNull(built);
        assertEquals(1L, built.getId());
        assertEquals("testuser", built.getUsername());
        assertEquals("test@example.com", built.getEmail());
        assertEquals("John", built.getFirstName());
        assertEquals("Doe", built.getLastName());
        assertEquals("password", built.getPassword());
        assertEquals(1, built.getEnabled());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User(1L, "testuser", "test@example.com", "John", "Doe", "password", 1, role);
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
    }

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    void testGettersAndSetters() {
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("password123");
        user.setEnabled(1);
        user.setRole(role);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("password123", user.getPassword());
        assertEquals(1, user.getEnabled());
        assertNotNull(user.getRole());
    }

    @Test
    void testGetColumnCount() {
        user.setRole(role);
        int columnCount = user.getColumnCount();
        assertTrue(columnCount > 0);
    }

    @Test
    void testGetRoleId() {
        user.setRole(role);
        assertEquals(1, user.getRole_id());
    }

    @Test
    void testGetRoleName() {
        user.setRole(role);
        assertEquals("ROLE_USER", user.getRole_name());
    }

    @Test
    void testGetName() {
        user.setFirstName("John");
        user.setLastName("Doe");
        assertEquals("John Doe", user.getName());
    }

    @Test
    void testGetNameWithNullValues() {
        user.setFirstName(null);
        user.setLastName(null);
        assertEquals("null null", user.getName());
    }

    @Test
    void testEmailValidation() {
        user.setEmail("test@example.com");
        assertTrue(user.getEmail().contains("@"));
    }

    @Test
    void testEnabledStatus() {
        user.setEnabled(1);
        assertEquals(1, user.getEnabled());

        user.setEnabled(0);
        assertEquals(0, user.getEnabled());
    }

    @Test
    void testUserEntity() {
        assertTrue(user.getClass().isAnnotationPresent(jakarta.persistence.Entity.class));
    }

    @Test
    void testUserHasIdAnnotation() throws NoSuchFieldException {
        assertTrue(User.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.Id.class));
    }

    @Test
    void testUserHasGeneratedValueAnnotation() throws NoSuchFieldException {
        assertTrue(User.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.GeneratedValue.class));
    }

    @Test
    void testRoleRelationship() {
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");
        user.setRole(adminRole);

        assertEquals(2, user.getRole_id());
        assertEquals("ROLE_ADMIN", user.getRole_name());
    }
}
