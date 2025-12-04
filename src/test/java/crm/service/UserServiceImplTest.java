package crm.service;

import crm.entity.Role;
import crm.entity.User;
import crm.repository.RoleRepository;
import crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
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
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = User.builder()
                .id(2L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .enabled(1)
                .role(role)
                .build();
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        User result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should list all users")
    void testListAllUsers() {
        when(userRepository.findAllByEnabled(1)).thenReturn(Arrays.asList(user));

        Iterable<User> result = userService.listAllUsers();

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(userRepository, times(1)).findAllByEnabled(1);
    }

    @Test
    @DisplayName("Should show user by id")
    void testShowUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        User result = userService.showUser(2L);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should return null when user not found")
    void testShowUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userService.showUser(999L);

        assertNull(result);
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should edit user")
    void testEditUser() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.editUser(user);

        assertEquals(1, user.getEnabled());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should delete user")
    void testDeleteUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.deleteUser(user);

        assertEquals(0, user.getEnabled());
        assertNull(user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should handle edit user with null role")
    void testEditUserNullRole() {
        user.setRole(null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.editUser(user);

        assertNotNull(user.getRole());
        assertEquals("ROLE_USER", user.getRole().getName());
        verify(roleRepository, atLeastOnce()).findByName("ROLE_USER");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should handle null username")
    void testFindByUsernameNull() {
        when(userRepository.findByUsername(null)).thenReturn(null);

        User result = userService.findByUsername(null);

        assertNull(result);
        verify(userRepository, times(1)).findByUsername(null);
    }
}
