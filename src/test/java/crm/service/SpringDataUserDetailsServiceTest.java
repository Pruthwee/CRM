package crm.service;

import crm.entity.CurrentUser;
import crm.entity.Role;
import crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringDataUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SpringDataUserDetailsService userDetailsService;

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
                .enabled(1)
                .role(testRole)
                .build();
    }

    @Test
    void testLoadUserByUsername() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertTrue(result instanceof CurrentUser);
        assertEquals("testuser", result.getUsername());
        verify(userService).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });

        verify(userService).findByUsername("nonexistent");
    }

    @Test
    void testLoadUserByUsernameNull() {
        when(userService.findByUsername(null)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(null);
        });
    }

    @Test
    void testLoadUserByUsernameEmpty() {
        when(userService.findByUsername("")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("");
        });
    }

    @Test
    void testLoadUserByUsernameWithAuthorities() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(result.getAuthorities());
        assertFalse(result.getAuthorities().isEmpty());
    }

    @Test
    void testLoadUserByUsernameWithCorrectRole() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsernameWithAdminRole() {
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");
        testUser.setRole(adminRole);

        when(userService.findByUsername("admin")).thenReturn(testUser);

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsernameReturnsCurrentUser() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertTrue(result instanceof CurrentUser);
        CurrentUser currentUser = (CurrentUser) result;
        assertEquals(testUser.getUsername(), currentUser.getUser().getUsername());
    }
}
