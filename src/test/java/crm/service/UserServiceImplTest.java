package crm.service;

import crm.entity.Role;
import crm.entity.User;
import crm.repository.RoleRepository;
import crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testRole = new Role();
        testRole.setId(1);
        testRole.setName("ROLE_USER");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .enabled(1)
                .role(testRole)
                .build();
    }

    @Test
    void testSetUserRepository() {
        UserRepository repo = mock(UserRepository.class);
        userService.setUserRepository(repo);
        // Verify setter works without exception
        assertDoesNotThrow(() -> userService.setUserRepository(repo));
    }

    @Test
    void testSetRoleRepository() {
        RoleRepository repo = mock(RoleRepository.class);
        assertDoesNotThrow(() -> userService.setRoleRepository(repo));
    }

    @Test
    void testSetPasswordEncoder() {
        BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
        assertDoesNotThrow(() -> userService.setPasswordEncoder(encoder));
    }

    @Test
    void testSetAuthenticationManager() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        assertDoesNotThrow(() -> userService.setAuthenticationManager(manager));
    }

    @Test
    void testSetSpringDataUserDetailsService() {
        SpringDataUserDetailsService service = mock(SpringDataUserDetailsService.class);
        assertDoesNotThrow(() -> userService.setSpringDataUserDetailsService(service));
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        User result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        User result = userService.findByUsername("nonexistent");

        assertNull(result);
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void testFindByUsernameNull() {
        when(userRepository.findByUsername(null)).thenReturn(null);

        User result = userService.findByUsername(null);

        assertNull(result);
        verify(userRepository).findByUsername(null);
    }

    @Test
    void testListAllUsers() {
        when(userRepository.findAllByEnabled(1)).thenReturn(Arrays.asList(testUser));

        Iterable<User> result = userService.listAllUsers();

        assertNotNull(result);
        verify(userRepository).findAllByEnabled(1);
    }

    @Test
    void testListAllUsersEmpty() {
        when(userRepository.findAllByEnabled(1)).thenReturn(Arrays.asList());

        Iterable<User> result = userService.listAllUsers();

        assertNotNull(result);
        verify(userRepository).findAllByEnabled(1);
    }

    @Test
    void testShowUser() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

        User result = userService.showUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void testShowUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        User result = userService.showUser(999L);

        assertNull(result);
        verify(userRepository).findById(999L);
    }

    @Test
    void testShowUserNullId() {
        when(userRepository.findById(null)).thenReturn(java.util.Optional.empty());

        User result = userService.showUser(null);

        assertNull(result);
        verify(userRepository).findById(null);
    }

    @Test
    void testSaveUser() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(springDataUserDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));

        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .build();

        userService.saveUser(newUser);

        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode("password");
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void testSaveUserAsAdmin() {
        testUser.setId(1L);
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");

        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(adminRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(springDataUserDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));

        userService.saveUser(testUser);

        verify(roleRepository).findByName("ROLE_ADMIN");
    }

    @Test
    void testEditUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(anyInt())).thenReturn(java.util.Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.editUser(testUser);

        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void testEditUserWithNullRole() {
        testUser.setRole(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.editUser(testUser);

        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(testUser);
    }

    @Test
    void testEditUserWithNullPointerException() {
        testUser.setRole(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.editUser(testUser);

        verify(roleRepository).findByName("ROLE_USER");
    }

    @Test
    void testDeleteUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(testUser);

        assertEquals(0, testUser.getEnabled());
        assertNull(testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeleteUserSetsEnabledToZero() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(testUser);

        assertEquals(0, testUser.getEnabled());
    }

    @Test
    void testDeleteUserSetsPasswordToNull() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(testUser);

        assertNull(testUser.getPassword());
    }
}
