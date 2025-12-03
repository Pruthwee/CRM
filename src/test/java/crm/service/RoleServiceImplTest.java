package crm.service;

import crm.entity.Role;
import crm.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testRole = new Role();
        testRole.setId(1);
        testRole.setName("ROLE_USER");
    }

    @Test
    void testConstructor() {
        RoleRepository repo = mock(RoleRepository.class);
        RoleServiceImpl service = new RoleServiceImpl(repo);
        assertNotNull(service);
    }

    @Test
    void testListAllRoles() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(testRole));

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRolesEmpty() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRolesMultiple() {
        Role role2 = new Role();
        role2.setId(2);
        role2.setName("ROLE_ADMIN");

        when(roleRepository.findAll()).thenReturn(Arrays.asList(testRole, role2));

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        verify(roleRepository).findAll();
    }
}
