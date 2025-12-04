package crm.service;

import crm.entity.Role;
import crm.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl Tests")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");
    }

    @Test
    @DisplayName("Should list all roles")
    void testListAllRoles() {
        Role role2 = new Role();
        role2.setId(2);
        role2.setName("ROLE_ADMIN");

        List<Role> roles = Arrays.asList(role, role2);
        when(roleRepository.findAll()).thenReturn(roles);

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        assertEquals(2, ((List<Role>) result).size());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no roles")
    void testListAllRolesEmpty() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle single role")
    void testListAllRolesSingle() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role));

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        assertEquals(1, ((List<Role>) result).size());
        verify(roleRepository, times(1)).findAll();
    }
}
