package crm.service;

import crm.entity.CurrentUser;
import crm.entity.Role;
import crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringDataUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SpringDataUserDetailsService springDataUserDetailsService;

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
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .enabled(1)
                .role(role)
                .build();
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(userService).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_ReturnsCurrentUser() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        assertTrue(result instanceof CurrentUser);
    }

    @Test
    void testLoadUserByUsername_HasAuthorities() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        assertNotNull(result.getAuthorities());
        assertFalse(result.getAuthorities().isEmpty());
    }

    @Test
    void testLoadUserByUsername_AuthorityMatchesRole() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        boolean hasRole = result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        assertTrue(hasRole);
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        when(userService.findByUsername("unknown")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class,
                () -> springDataUserDetailsService.loadUserByUsername("unknown"));
        verify(userService).findByUsername("unknown");
    }

    @Test
    void testLoadUserByUsername_AdminRole() {
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");

        User adminUser = User.builder()
                .id(2L)
                .username("adminuser")
                .password("adminPass")
                .role(adminRole)
                .build();

        when(userService.findByUsername("adminuser")).thenReturn(adminUser);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("adminuser");
        assertNotNull(result);
        boolean hasAdminRole = result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        assertTrue(hasAdminRole);
    }

    @Test
    void testLoadUserByUsername_CurrentUserHasUser() {
        when(userService.findByUsername("testuser")).thenReturn(user);
        UserDetails result = springDataUserDetailsService.loadUserByUsername("testuser");
        CurrentUser currentUser = (CurrentUser) result;
        assertNotNull(currentUser.getUser());
        assertEquals(user, currentUser.getUser());
    }
}
