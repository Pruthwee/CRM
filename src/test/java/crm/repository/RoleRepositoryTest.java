package crm.repository;

import crm.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleRepository Tests")
class RoleRepositoryTest {

    @Mock
    private RoleRepository roleRepository;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");
    }

    @Test
    @DisplayName("Should find role by name")
    void testFindByName() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);

        Role result = roleRepository.findByName("ROLE_USER");

        assertNotNull(result);
        assertEquals("ROLE_USER", result.getName());
        verify(roleRepository, times(1)).findByName("ROLE_USER");
    }

    @Test
    @DisplayName("Should return null when role not found by name")
    void testFindByNameNotFound() {
        when(roleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(null);

        Role result = roleRepository.findByName("ROLE_NONEXISTENT");

        assertNull(result);
        verify(roleRepository, times(1)).findByName("ROLE_NONEXISTENT");
    }

    @Test
    @DisplayName("Should save role")
    void testSave() {
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role result = roleRepository.save(role);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("ROLE_USER", result.getName());
        verify(roleRepository, times(1)).save(role);
    }

    @Test
    @DisplayName("Should find role by id")
    void testFindById() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));

        Optional<Role> result = roleRepository.findById(1);

        assertTrue(result.isPresent());
        assertEquals("ROLE_USER", result.get().getName());
        verify(roleRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when role not found by id")
    void testFindByIdNotFound() {
        when(roleRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Role> result = roleRepository.findById(999);

        assertFalse(result.isPresent());
        verify(roleRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find all roles")
    void testFindAll() {
        Role role2 = new Role();
        role2.setId(2);
        role2.setName("ROLE_ADMIN");

        List<Role> roles = Arrays.asList(role, role2);
        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete role")
    void testDelete() {
        doNothing().when(roleRepository).delete(role);

        roleRepository.delete(role);

        verify(roleRepository, times(1)).delete(role);
    }

    @Test
    @DisplayName("Should delete role by id")
    void testDeleteById() {
        doNothing().when(roleRepository).deleteById(1);

        roleRepository.deleteById(1);

        verify(roleRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should check if role exists by id")
    void testExistsById() {
        when(roleRepository.existsById(1)).thenReturn(true);

        boolean result = roleRepository.existsById(1);

        assertTrue(result);
        verify(roleRepository, times(1)).existsById(1);
    }

    @Test
    @DisplayName("Should return false when role does not exist")
    void testExistsByIdFalse() {
        when(roleRepository.existsById(999)).thenReturn(false);

        boolean result = roleRepository.existsById(999);

        assertFalse(result);
        verify(roleRepository, times(1)).existsById(999);
    }

    @Test
    @DisplayName("Should count roles")
    void testCount() {
        when(roleRepository.count()).thenReturn(5L);

        long result = roleRepository.count();

        assertEquals(5L, result);
        verify(roleRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should handle null name in findByName")
    void testFindByNameNull() {
        when(roleRepository.findByName(null)).thenReturn(null);

        Role result = roleRepository.findByName(null);

        assertNull(result);
        verify(roleRepository, times(1)).findByName(null);
    }

    @Test
    @DisplayName("Should handle empty name in findByName")
    void testFindByNameEmpty() {
        when(roleRepository.findByName("")).thenReturn(null);

        Role result = roleRepository.findByName("");

        assertNull(result);
        verify(roleRepository, times(1)).findByName("");
    }

    @Test
    @DisplayName("Should delete all roles")
    void testDeleteAll() {
        doNothing().when(roleRepository).deleteAll();

        roleRepository.deleteAll();

        verify(roleRepository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("Should handle typical role names")
    void testTypicalRoleNames() {
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");

        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);

        Role result = roleRepository.findByName("ROLE_ADMIN");

        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getName());
        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
    }
}
