package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    private Role role;

    @BeforeEach
    public void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_ADMIN");
    }

    @Test
    public void testRoleConstructor() {
        assertNotNull(role);
        assertEquals(1, role.getId());
        assertEquals("ROLE_ADMIN", role.getName());
    }

    @Test
    public void testRoleDefaultConstructor() {
        Role emptyRole = new Role();
        assertNotNull(emptyRole);
    }

    @Test
    public void testSettersAndGetters() {
        role.setId(2);
        assertEquals(2, role.getId());

        role.setName("ROLE_USER");
        assertEquals("ROLE_USER", role.getName());
    }

    @Test
    public void testRoleWithNullName() {
        Role nullRole = new Role();
        nullRole.setId(3);
        nullRole.setName(null);

        assertEquals(3, nullRole.getId());
        assertNull(nullRole.getName());
    }

    @Test
    public void testRoleIdUpdate() {
        role.setId(10);
        assertEquals(10, role.getId());
    }

    @Test
    public void testRoleNameUpdate() {
        role.setName("ROLE_MANAGER");
        assertEquals("ROLE_MANAGER", role.getName());
    }

    @Test
    public void testRoleEquality() {
        Role role1 = new Role();
        role1.setId(1);
        role1.setName("ROLE_ADMIN");

        Role role2 = new Role();
        role2.setId(1);
        role2.setName("ROLE_ADMIN");

        assertEquals(role1, role2);
    }

    @Test
    public void testRoleToString() {
        String roleString = role.toString();
        assertNotNull(roleString);
        assertTrue(roleString.contains("ROLE_ADMIN"));
    }

    @Test
    public void testRoleNameUnique() {
        role.setName("ROLE_OWNER");
        assertEquals("ROLE_OWNER", role.getName());
    }

    @Test
    public void testRoleDifferentIds() {
        Role role1 = new Role();
        role1.setId(1);
        role1.setName("ROLE_USER");

        Role role2 = new Role();
        role2.setId(2);
        role2.setName("ROLE_USER");

        assertNotEquals(role1.getId(), role2.getId());
    }
}
