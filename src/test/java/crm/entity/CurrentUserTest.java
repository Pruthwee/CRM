package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CurrentUserTest {

    private CurrentUser currentUser;

    @BeforeEach
    public void setUp() {
        currentUser = new CurrentUser();
    }

    @Test
    public void testCurrentUserConstructor() {
        assertNotNull(currentUser);
    }

    @Test
    public void testSetAndGetUser() {
        User user = new User();
        currentUser.setUser(user);
        assertEquals(user, currentUser.getUser());
    }

    @Test
    public void testSetAndGetAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        currentUser.setAuthorities(authorities);
        assertEquals(authorities, currentUser.getAuthorities());
    }

    @Test
    public void testGetPassword() {
        User user = new User();
        user.setPassword("testPassword");
        currentUser.setUser(user);
        assertEquals("testPassword", currentUser.getPassword());
    }

    @Test
    public void testGetUsername() {
        User user = new User();
        user.setUsername("testUser");
        currentUser.setUser(user);
        assertEquals("testUser", currentUser.getUsername());
    }

    @Test
    public void testIsAccountNonExpired() {
        assertTrue(currentUser.isAccountNonExpired());
    }

    @Test
    public void testIsAccountNonLocked() {
        assertTrue(currentUser.isAccountNonLocked());
    }

    @Test
    public void testIsCredentialsNonExpired() {
        assertTrue(currentUser.isCredentialsNonExpired());
    }

    @Test
    public void testIsEnabled() {
        assertTrue(currentUser.isEnabled());
    }

    @Test
    public void testGetAuthoritiesNotNull() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        currentUser.setAuthorities(authorities);
        assertNotNull(currentUser.getAuthorities());
    }

    @Test
    public void testGetAuthoritiesEmpty() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        currentUser.setAuthorities(authorities);
        assertTrue(currentUser.getAuthorities().isEmpty());
    }

    @Test
    public void testGetAuthoritiesWithMultipleRoles() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        currentUser.setAuthorities(authorities);
        assertEquals(2, currentUser.getAuthorities().size());
    }
}
