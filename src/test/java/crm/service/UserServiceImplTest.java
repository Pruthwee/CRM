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
import java.util.List;
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

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("rawPassword")
                .enabled(1)
                .role(userRole)
                .build();
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        User result = userService.findByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);
        User result = userService.findByUsername("unknown");
        assertNull(result);
        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void testListAllUsers() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAllByEnabled(1)).thenReturn(users);
        Iterable<User> result = userService.listAllUsers();
        assertNotNull(result);
        verify(userRepository).findAllByEnabled(1);
    }

    @Test
    void testShowUser_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userService.showUser(1L);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void testShowUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        User result = userService.showUser(99L);
        assertNull(result);
        verify(userRepository).findById(99L);
    }

    @Test
    void testSaveUser_NonAdminUser() {
        User newUser = User.builder()
                .id(2L)
                .username("newuser")
                .email("new@example.com")
                .password("rawPass")
                .build();

        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        UserDetails mockDetails = mock(UserDetails.class);
        when(mockDetails.getAuthorities()).thenReturn(new java.util.HashSet<>());
        when(springDataUserDetailsService.loadUserByUsername("newuser")).thenReturn(mockDetails);
        when(authenticationManager.authenticate(any())).thenReturn(null);

        userService.saveUser(newUser);

        assertEquals("encodedPass", newUser.getPassword());
        assertEquals(1, newUser.getEnabled());
        verify(userRepository, atLeastOnce()).save(newUser);
    }

    @Test
    void testEditUser_WithValidRole() {
        user.setPassword("rawPassword");
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(roleRepository.findById(1)).thenReturn(Optional.of(userRole));

        userService.editUser(user);

        assertEquals("encodedPassword", user.getPassword());
        assertEquals(1, user.getEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void testEditUser_WithNullRole() {
        User userWithNullRole = User.builder()
                .id(3L)
                .username("nullroleuser")
                .password("rawPass")
                .role(null)
                .build();

        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);

        userService.editUser(userWithNullRole);

        assertEquals("encodedPass", userWithNullRole.getPassword());
        assertEquals(1, userWithNullRole.getEnabled());
        verify(userRepository).save(userWithNullRole);
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(user);

        assertEquals(0, user.getEnabled());
        assertNull(user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testDeleteUser_SetsEnabledToZero() {
        user.setEnabled(1);
        userService.deleteUser(user);
        assertEquals(0, user.getEnabled());
    }

    @Test
    void testDeleteUser_SetsPasswordToNull() {
        user.setPassword("somePassword");
        userService.deleteUser(user);
        assertNull(user.getPassword());
    }
}
