package crm.service;

import crm.entity.Role;
import crm.entity.User;
import crm.repository.RoleRepository;
import crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SpringDataUserDetailsService springDataUserDetailsService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1);
        userRole.setName("ROLE_USER");

        adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("plainpassword");
        user.setEnabled(1);
        user.setRole(userRole);
    }

    @Test
    void testFindByUsername_found() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        User result = userService.findByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_notFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);
        User result = userService.findByUsername("unknown");
        assertNull(result);
        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void testListAllUsers() {
        when(userRepository.findAllByEnabled(1)).thenReturn(Arrays.asList(user));
        Iterable<User> result = userService.listAllUsers();
        assertNotNull(result);
        verify(userRepository).findAllByEnabled(1);
    }

    @Test
    void testShowUser_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userService.showUser(1L);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void testShowUser_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        User result = userService.showUser(99L);
        assertNull(result);
        verify(userRepository).findById(99L);
    }

    @Test
    void testSaveUser_setsRoleAndEnabled() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(passwordEncoder.encode("plainpassword")).thenReturn("encodedpassword");
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getAuthorities()).thenReturn(new java.util.HashSet<>());
        when(springDataUserDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);
        when(authenticationManager.authenticate(any())).thenReturn(null);

        userService.saveUser(user);

        assertEquals(1, user.getEnabled());
        assertEquals("encodedpassword", user.getPassword());
        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void testEditUser_withValidRole() {
        user.setRole(userRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(roleRepository.findById(1)).thenReturn(Optional.of(userRole));

        userService.editUser(user);

        assertEquals("encodedpassword", user.getPassword());
        assertEquals(1, user.getEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void testEditUser_withNullRole_fallsBackToRoleUser() {
        user.setRole(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);

        userService.editUser(user);

        assertEquals(userRole, user.getRole());
        assertEquals(1, user.getEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void testDeleteUser_setsEnabledZeroAndNullPassword() {
        userService.deleteUser(user);
        assertEquals(0, user.getEnabled());
        assertNull(user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testDeleteUser_callsRepositorySave() {
        userService.deleteUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testSetUserRepository() {
        userService.setUserRepository(userRepository);
        assertNotNull(userService);
    }

    @Test
    void testSetRoleRepository() {
        userService.setRoleRepository(roleRepository);
        assertNotNull(userService);
    }

    @Test
    void testSetPasswordEncoder() {
        userService.setPasswordEncoder(passwordEncoder);
        assertNotNull(userService);
    }

    @Test
    void testSetAuthenticationManager() {
        userService.setAuthenticationManager(authenticationManager);
        assertNotNull(userService);
    }

    @Test
    void testSetSpringDataUserDetailsService() {
        userService.setSpringDataUserDetailsService(springDataUserDetailsService);
        assertNotNull(userService);
    }
}
