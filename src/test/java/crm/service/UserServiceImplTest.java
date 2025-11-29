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

import java.util.ArrayList;
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

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1);
        testRole.setName("ROLE_USER");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .enabled(1)
                .role(testRole)
                .build();
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
    void testListAllUsers() {
        Iterable<User> users = new ArrayList<>();
        when(userRepository.findAllByEnabled(1)).thenReturn(users);

        Iterable<User> result = userService.listAllUsers();

        assertNotNull(result);
        verify(userRepository).findAllByEnabled(1);
    }

    @Test
    void testShowUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.showUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void testShowUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userService.showUser(999L);

        assertNull(result);
        verify(userRepository).findById(999L);
    }

    @Test
    void testEditUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(anyInt())).thenReturn(Optional.of(testRole));

        userService.editUser(testUser);

        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void testEditUserWithNullRole() {
        testUser.setRole(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);

        userService.editUser(testUser);

        verify(passwordEncoder).encode(anyString());
        verify(roleRepository, atLeastOnce()).findByName("ROLE_USER");
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(testUser);

        assertEquals(0, testUser.getEnabled());
        assertNull(testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void testSetUserRepository() {
        UserRepository newRepo = mock(UserRepository.class);
        userService.setUserRepository(newRepo);
        assertDoesNotThrow(() -> userService.setUserRepository(newRepo));
    }

    @Test
    void testSetRoleRepository() {
        RoleRepository newRepo = mock(RoleRepository.class);
        assertDoesNotThrow(() -> userService.setRoleRepository(newRepo));
    }

    @Test
    void testSetPasswordEncoder() {
        BCryptPasswordEncoder newEncoder = mock(BCryptPasswordEncoder.class);
        assertDoesNotThrow(() -> userService.setPasswordEncoder(newEncoder));
    }

    @Test
    void testSetAuthenticationManager() {
        AuthenticationManager newManager = mock(AuthenticationManager.class);
        assertDoesNotThrow(() -> userService.setAuthenticationManager(newManager));
    }

    @Test
    void testSetSpringDataUserDetailsService() {
        SpringDataUserDetailsService newService = mock(SpringDataUserDetailsService.class);
        assertDoesNotThrow(() -> userService.setSpringDataUserDetailsService(newService));
    }
}
