package crm.service;

import crm.entity.CurrentUser;
import crm.entity.Role;
import crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpringDataUserDetailsService Tests")
class SpringDataUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SpringDataUserDetailsService userDetailsService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .enabled(1)
                .role(role)
                .build();
    }

    @Test
    @DisplayName("Should load user by username")
    void testLoadUserByUsername() {
        when(userService.findByUsername("testuser")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertTrue(result instanceof CurrentUser);
        assertEquals("testuser", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertNotNull(result.getAuthorities());
        assertEquals(1, result.getAuthorities().size());
        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLoadUserByUsernameNotFound() {
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("nonexistent"));

        verify(userService, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should set correct authorities")
    void testLoadUserByUsernameAuthorities() {
        when(userService.findByUsername("testuser")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Should handle admin role")
    void testLoadUserByUsernameAdmin() {
        role.setName("ROLE_ADMIN");
        user.setRole(role);
        when(userService.findByUsername("admin")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should throw exception for null username")
    void testLoadUserByUsernameNull() {
        when(userService.findByUsername(null)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(null));
    }

    @Test
    @DisplayName("Should throw exception for empty username")
    void testLoadUserByUsernameEmpty() {
        when(userService.findByUsername("")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(""));
    }
}
