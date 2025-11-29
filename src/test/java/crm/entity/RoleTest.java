package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
    }

    @Test
    void testGettersAndSetters() {
        role.setId(1);
        role.setName("ROLE_USER");

        assertEquals(1, role.getId());
        assertEquals("ROLE_USER", role.getName());
    }

    @Test
    void testSetId() {
        role.setId(5);
        assertEquals(5, role.getId());
    }

    @Test
    void testSetName() {
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
    void testRoleEntity() {
        assertNotNull(role);
        assertTrue(role.getClass().isAnnotationPresent(jakarta.persistence.Entity.class));
    }

    @Test
    void testRoleHasIdAnnotation() throws NoSuchFieldException {
        assertTrue(Role.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.Id.class));
    }

    @Test
    void testRoleHasGeneratedValueAnnotation() throws NoSuchFieldException {
        assertTrue(Role.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.GeneratedValue.class));
    }

    @Test
    void testRoleHasTableAnnotation() {
        assertTrue(Role.class.isAnnotationPresent(jakarta.persistence.Table.class));
    }

    @Test
    void testRoleNameColumnHasUniqueConstraint() throws NoSuchFieldException {
        jakarta.persistence.Column column = Role.class.getDeclaredField("name").getAnnotation(jakarta.persistence.Column.class);
        assertNotNull(column);
        assertTrue(column.unique());
    }

    @Test
    void testMultipleRoles() {
        Role role1 = new Role();
        role1.setId(1);
        role1.setName("ROLE_USER");

        Role role2 = new Role();
        role2.setId(2);
        role2.setName("ROLE_ADMIN");

        assertNotEquals(role1.getId(), role2.getId());
        assertNotEquals(role1.getName(), role2.getName());
    }
}
