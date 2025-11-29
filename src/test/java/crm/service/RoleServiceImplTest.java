package crm.service;

import crm.entity.Role;
import crm.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1);
        testRole.setName("ROLE_USER");
    }

    @Test
    void testConstructor() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository);
        assertNotNull(service);
    }

    @Test
    void testListAllRoles() {
        List<Role> roles = new ArrayList<>();
        roles.add(testRole);
        when(roleRepository.findAll()).thenReturn(roles);

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRolesEmpty() {
        when(roleRepository.findAll()).thenReturn(new ArrayList<>());

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRolesMultiple() {
        List<Role> roles = new ArrayList<>();
        Role role1 = new Role();
        role1.setId(1);
        role1.setName("ROLE_USER");
        Role role2 = new Role();
        role2.setId(2);
        role2.setName("ROLE_ADMIN");
        roles.add(role1);
        roles.add(role2);

        when(roleRepository.findAll()).thenReturn(roles);

        Iterable<Role> result = roleService.listAllRoles();

        assertNotNull(result);
        verify(roleRepository).findAll();
    }
}
