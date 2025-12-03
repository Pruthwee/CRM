package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");
    }

    @Test
    void testConstructor() {
        Role newRole = new Role();
        assertNotNull(newRole);
    }

    @Test
    void testId() {
        assertEquals(1, role.getId());
        role.setId(2);
        assertEquals(2, role.getId());
    }

    @Test
    void testName() {
        assertEquals("ROLE_USER", role.getName());
        role.setName("ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", role.getName());
    }

    @Test
    void testNullName() {
        role.setName(null);
        assertNull(role.getName());
    }

    @Test
    void testEmptyName() {
        role.setName("");
        assertEquals("", role.getName());
    }

    @Test
    void testGettersAndSetters() {
        role.setId(5);
        role.setName("ROLE_MANAGER");

        assertEquals(5, role.getId());
        assertEquals("ROLE_MANAGER", role.getName());
    }
}
