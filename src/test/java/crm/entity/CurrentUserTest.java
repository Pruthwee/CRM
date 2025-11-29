package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
                .password("encodedPassword")
                .email("test@example.com")
                .enabled(1)
                .role(role)
                .build();

        authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void testGettersAndSetters() {
        currentUser.setUser(user);
        currentUser.setAuthorities(authorities);

        assertNotNull(currentUser.getUser());
        assertNotNull(currentUser.getAuthorities());
        assertEquals(user, currentUser.getUser());
        assertEquals(authorities, currentUser.getAuthorities());
    }

    @Test
    void testGetAuthorities() {
        currentUser.setAuthorities(authorities);

        assertEquals(1, currentUser.getAuthorities().size());
        assertTrue(currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testGetPassword() {
        currentUser.setUser(user);
        assertEquals("encodedPassword", currentUser.getPassword());
    }

    @Test
    void testGetUsername() {
        currentUser.setUser(user);
        assertEquals("testuser", currentUser.getUsername());
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
    void testGetPasswordWithNullUser() {
        currentUser.setUser(null);
        assertThrows(NullPointerException.class, () -> currentUser.getPassword());
    }

    @Test
    void testGetUsernameWithNullUser() {
        currentUser.setUser(null);
        assertThrows(NullPointerException.class, () -> currentUser.getUsername());
    }

    @Test
    void testMultipleAuthorities() {
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        currentUser.setAuthorities(authorities);

        assertEquals(2, currentUser.getAuthorities().size());
    }

    @Test
    void testEmptyAuthorities() {
        currentUser.setAuthorities(new HashSet<>());
        assertTrue(currentUser.getAuthorities().isEmpty());
    }
}
