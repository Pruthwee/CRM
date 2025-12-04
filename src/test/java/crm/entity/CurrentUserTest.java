package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CurrentUser Entity Tests")
class CurrentUserTest {

    private CurrentUser currentUser;
    private User user;
    private Set<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        currentUser = new CurrentUser();

        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(1)
                .role(role)
                .build();

        authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    @DisplayName("Should create CurrentUser with default constructor")
    void testDefaultConstructor() {
        assertNotNull(currentUser);
        assertNull(currentUser.getUser());
        assertNull(currentUser.getAuthorities());
    }

    @Test
    @DisplayName("Should set and get user correctly")
    void testSetAndGetUser() {
        currentUser.setUser(user);
        assertEquals(user, currentUser.getUser());
        assertEquals("testuser", currentUser.getUsername());
        assertEquals("password123", currentUser.getPassword());
    }

    @Test
    @DisplayName("Should set and get authorities correctly")
    void testSetAndGetAuthorities() {
        currentUser.setAuthorities(authorities);
        assertEquals(authorities, currentUser.getAuthorities());
        assertEquals(1, currentUser.getAuthorities().size());
        assertTrue(currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Should get username from user")
    void testGetUsername() {
        currentUser.setUser(user);
        String username = currentUser.getUsername();
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should get password from user")
    void testGetPassword() {
        currentUser.setUser(user);
        String password = currentUser.getPassword();
        assertEquals("password123", password);
    }

    @Test
    @DisplayName("Should return true for isAccountNonExpired")
    void testIsAccountNonExpired() {
        boolean result = currentUser.isAccountNonExpired();
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for isAccountNonLocked")
    void testIsAccountNonLocked() {
        boolean result = currentUser.isAccountNonLocked();
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for isCredentialsNonExpired")
    void testIsCredentialsNonExpired() {
        boolean result = currentUser.isCredentialsNonExpired();
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for isEnabled")
    void testIsEnabled() {
        boolean result = currentUser.isEnabled();
        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void testNullUser() {
        currentUser.setUser(null);
        assertNull(currentUser.getUser());
        assertThrows(NullPointerException.class, () -> currentUser.getUsername());
        assertThrows(NullPointerException.class, () -> currentUser.getPassword());
    }

    @Test
    @DisplayName("Should handle null authorities")
    void testNullAuthorities() {
        currentUser.setAuthorities(null);
        assertNull(currentUser.getAuthorities());
    }

    @Test
    @DisplayName("Should handle empty authorities")
    void testEmptyAuthorities() {
        Set<GrantedAuthority> emptyAuthorities = new HashSet<>();
        currentUser.setAuthorities(emptyAuthorities);
        assertNotNull(currentUser.getAuthorities());
        assertTrue(currentUser.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple authorities")
    void testMultipleAuthorities() {
        Set<GrantedAuthority> multipleAuthorities = new HashSet<>();
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));

        currentUser.setAuthorities(multipleAuthorities);

        assertEquals(3, currentUser.getAuthorities().size());
        assertTrue(currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    @Test
    @DisplayName("Should test complete CurrentUser setup")
    void testCompleteSetup() {
        currentUser.setUser(user);
        currentUser.setAuthorities(authorities);

        assertNotNull(currentUser.getUser());
        assertEquals("testuser", currentUser.getUsername());
        assertEquals("password123", currentUser.getPassword());
        assertEquals(authorities, currentUser.getAuthorities());
        assertTrue(currentUser.isAccountNonExpired());
        assertTrue(currentUser.isAccountNonLocked());
        assertTrue(currentUser.isCredentialsNonExpired());
        assertTrue(currentUser.isEnabled());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        CurrentUser currentUser1 = new CurrentUser();
        currentUser1.setUser(user);
        currentUser1.setAuthorities(authorities);

        CurrentUser currentUser2 = new CurrentUser();
        currentUser2.setUser(user);
        currentUser2.setAuthorities(authorities);

        assertEquals(currentUser1, currentUser2);
        assertEquals(currentUser1.hashCode(), currentUser2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        currentUser.setUser(user);
        currentUser.setAuthorities(authorities);
        String result = currentUser.toString();
        assertNotNull(result);
        assertTrue(result.contains("CurrentUser"));
    }

    @Test
    @DisplayName("Should handle user with null password")
    void testUserWithNullPassword() {
        user.setPassword(null);
        currentUser.setUser(user);
        String password = currentUser.getPassword();
        assertNull(password);
    }

    @Test
    @DisplayName("Should handle user with empty password")
    void testUserWithEmptyPassword() {
        user.setPassword("");
        currentUser.setUser(user);
        String password = currentUser.getPassword();
        assertEquals("", password);
    }

    @Test
    @DisplayName("Should handle user with null username")
    void testUserWithNullUsername() {
        user.setUsername(null);
        currentUser.setUser(user);
        String username = currentUser.getUsername();
        assertNull(username);
    }
}
