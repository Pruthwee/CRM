package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    private Role role;

    @BeforeEach
    public void setUp() {
        role = new Role();
    }

    @Test
    public void testRoleConstructor() {
        assertNotNull(role);
    }

    @Test
    public void testSetAndGetId() {
        role.setId(1);
        assertEquals(1, role.getId());
    }

    @Test
    public void testSetAndGetName() {
        role.setName("ROLE_USER");
        assertEquals("ROLE_USER", role.getName());
    }

    @Test
    public void testRoleWithNullName() {
        role.setName(null);
        assertNull(role.getName());
    }

    @Test
    public void testRoleWithZeroId() {
        role.setId(0);
        assertEquals(0, role.getId());
    }

    @Test
    public void testRoleWithNegativeId() {
        role.setId(-1);
        assertEquals(-1, role.getId());
    }
}
