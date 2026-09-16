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

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(1);
        user.setRole(role);

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
    void testSetAndGetUser() {
        assertEquals(user, currentUser.getUser());
    }

    @Test
    void testSetAndGetAuthorities() {
        assertEquals(authorities, currentUser.getAuthorities());
    }

    @Test
    void testGetPassword_returnsUserPassword() {
        assertEquals("encodedPassword", currentUser.getPassword());
    }

    @Test
    void testGetUsername_returnsUserUsername() {
        assertEquals("testuser", currentUser.getUsername());
    }

    @Test
    void testIsAccountNonExpired_returnsTrue() {
        assertTrue(currentUser.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked_returnsTrue() {
        assertTrue(currentUser.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired_returnsTrue() {
        assertTrue(currentUser.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled_returnsTrue() {
        assertTrue(currentUser.isEnabled());
    }

    @Test
    void testGetAuthorities_returnsCorrectAuthority() {
        Collection<? extends GrantedAuthority> auths = currentUser.getAuthorities();
        assertNotNull(auths);
        assertEquals(1, auths.size());
        assertTrue(auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testSetUser_updatesPassword() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        currentUser.setUser(newUser);
        assertEquals("newpassword", currentUser.getPassword());
    }

    @Test
    void testSetUser_updatesUsername() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        currentUser.setUser(newUser);
        assertEquals("newuser", currentUser.getUsername());
    }

    @Test
    void testSetAuthorities_multipleRoles() {
        Set<GrantedAuthority> multipleAuthorities = new HashSet<>();
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        multipleAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        currentUser.setAuthorities(multipleAuthorities);
        assertEquals(2, currentUser.getAuthorities().size());
    }

    @Test
    void testEqualsAndHashCode() {
        CurrentUser cu1 = new CurrentUser();
        cu1.setUser(user);
        cu1.setAuthorities(authorities);

        CurrentUser cu2 = new CurrentUser();
        cu2.setUser(user);
        cu2.setAuthorities(authorities);

        assertEquals(cu1, cu2);
    }

    @Test
    void testToString() {
        String str = currentUser.toString();
        assertNotNull(str);
    }
}
