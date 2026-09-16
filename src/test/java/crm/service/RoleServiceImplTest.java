package crm.service;

import crm.entity.Role;
import crm.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role roleUser;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        roleUser = new Role();
        roleUser.setId(1);
        roleUser.setName("ROLE_USER");

        roleAdmin = new Role();
        roleAdmin.setId(2);
        roleAdmin.setName("ROLE_ADMIN");
    }

    @Test
    void testConstructor() {
        RoleServiceImpl service = new RoleServiceImpl(roleRepository);
        assertNotNull(service);
    }

    @Test
    void testListAllRoles_returnsAllRoles() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleUser, roleAdmin));
        Iterable<Role> result = roleService.listAllRoles();
        assertNotNull(result);
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRoles_emptyList() {
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        Iterable<Role> result = roleService.listAllRoles();
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRoles_singleRole() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleUser));
        Iterable<Role> result = roleService.listAllRoles();
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(roleRepository).findAll();
    }

    @Test
    void testListAllRoles_callsRepositoryFindAll() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleUser, roleAdmin));
        roleService.listAllRoles();
        verify(roleRepository, times(1)).findAll();
    }
}
