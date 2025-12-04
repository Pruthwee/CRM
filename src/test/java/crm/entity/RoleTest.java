package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role Entity Tests")
class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
    }

    @Test
    @DisplayName("Should create role with default constructor")
    void testDefaultConstructor() {
        assertNotNull(role);
        assertEquals(0, role.getId());
        assertNull(role.getName());
    }

    @Test
    @DisplayName("Should set and get id correctly")
    void testSetAndGetId() {
        int expectedId = 1;
        role.setId(expectedId);
        assertEquals(expectedId, role.getId());
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        String expectedName = "ROLE_ADMIN";
        role.setName(expectedName);
        assertEquals(expectedName, role.getName());
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        role.setName(null);
        assertNull(role.getName());
    }

    @Test
    @DisplayName("Should handle empty name")
    void testEmptyName() {
        String emptyName = "";
        role.setName(emptyName);
        assertEquals(emptyName, role.getName());
    }

    @Test
    @DisplayName("Should handle long name")
    void testLongName() {
        String longName = "ROLE_" + "A".repeat(1000);
        role.setName(longName);
        assertEquals(longName, role.getName());
    }

    @Test
    @DisplayName("Should handle negative id")
    void testNegativeId() {
        role.setId(-1);
        assertEquals(-1, role.getId());
    }

    @Test
    @DisplayName("Should handle zero id")
    void testZeroId() {
        role.setId(0);
        assertEquals(0, role.getId());
    }

    @Test
    @DisplayName("Should handle large id")
    void testLargeId() {
        role.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, role.getId());
    }

    @Test
    @DisplayName("Should test equals and hashCode consistency")
    void testEqualsAndHashCode() {
        Role role1 = new Role();
        role1.setId(1);
        role1.setName("ROLE_USER");

        Role role2 = new Role();
        role2.setId(1);
        role2.setName("ROLE_USER");

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        role.setId(1);
        role.setName("ROLE_MANAGER");
        String result = role.toString();
        assertNotNull(result);
        assertTrue(result.contains("Role"));
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        String specialName = "ROLE_ADMIN-2024";
        role.setName(specialName);
        assertEquals(specialName, role.getName());
    }

    @Test
    @DisplayName("Should handle typical role names")
    void testTypicalRoleNames() {
        String[] roleNames = {"ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_GUEST"};

        for (String roleName : roleNames) {
            role.setName(roleName);
            assertEquals(roleName, role.getName());
        }
    }
}
