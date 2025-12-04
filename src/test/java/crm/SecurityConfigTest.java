package crm;

import crm.service.SpringDataUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    public void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Test
    public void testSecurityConfigConstructor() {
        assertNotNull(securityConfig);
    }

    @Test
    public void testPasswordEncoderNotNull() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
    }

    @Test
    public void testPasswordEncoderEncodesPasswords() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword";
        String encodedPassword = encoder.encode(rawPassword);
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
    }

    @Test
    public void testPasswordEncoderMatches() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword";
        String encodedPassword = encoder.encode(rawPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    public void testPasswordEncoderDoesNotMatchWrongPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = encoder.encode(rawPassword);
        assertFalse(encoder.matches(wrongPassword, encodedPassword));
    }

    @Test
    public void testCustomUserDetailsServiceNotNull() {
        SpringDataUserDetailsService service = securityConfig.customUserDetailsService();
        assertNotNull(service);
    }
}
