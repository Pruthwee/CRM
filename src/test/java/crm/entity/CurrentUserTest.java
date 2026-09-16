package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserTest {

    private CurrentUser currentUser;
    private User user;
    private Set<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        Role role = new Role();
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

        authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        currentUser = new CurrentUser();
        currentUser.setUser(user);
        currentUser.setAuthorities(authorities);
    }

    @Test
    void testDefaultConstructor() {
        CurrentUser cu = new CurrentUser();
        assertNotNull(cu);
    }

    @Test
    void testGetPassword() {
        assertEquals("encodedPassword", currentUser.getPassword());
    }

    @Test
    void testGetUsername() {
        assertEquals("testuser", currentUser.getUsername());
    }

    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> auths = currentUser.getAuthorities();
        assertNotNull(auths);
        assertEquals(1, auths.size());
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(currentUser.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(currentUser.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(currentUser.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        assertTrue(currentUser.isEnabled());
    }

    @Test
    void testSetAndGetUser() {
        User newUser = User.builder()
                .id(2L)
                .username("newuser")
                .password("newpass")
                .build();
        currentUser.setUser(newUser);
        assertEquals(newUser, currentUser.getUser());
    }

    @Test
    void testSetAndGetAuthorities() {
        Set<GrantedAuthority> newAuthorities = new HashSet<>();
        newAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        currentUser.setAuthorities(newAuthorities);
        assertEquals(newAuthorities, currentUser.getAuthorities());
    }

    @Test
    void testAuthoritiesContainRole() {
        boolean hasRole = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        assertTrue(hasRole);
    }

    @Test
    void testToString() {
        String str = currentUser.toString();
        assertNotNull(str);
    }
}
