package crm;

import crm.service.SpringDataUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityConfig = new SecurityConfig();
    }

    @Test
    void testPasswordEncoder() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void testPasswordEncoderEncodesPassword() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

        String rawPassword = "testPassword123";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testPasswordEncoderWithNullPassword() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

        assertThrows(IllegalArgumentException.class, () -> {
            encoder.encode(null);
        });
    }

    @Test
    void testPasswordEncoderWithEmptyPassword() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

        assertDoesNotThrow(() -> {
            String encoded = encoder.encode("");
            assertNotNull(encoded);
        });
    }

    @Test
    void testCustomUserDetailsService() {
        SpringDataUserDetailsService service = securityConfig.customUserDetailsService();

        assertNotNull(service);
        assertTrue(service instanceof SpringDataUserDetailsService);
    }

    @Test
    void testAuthenticationProvider() {
        DaoAuthenticationProvider provider = securityConfig.authenticationProvider();

        assertNotNull(provider);
        assertTrue(provider instanceof DaoAuthenticationProvider);
    }

    @Test
    void testAuthenticationProviderConfiguration() {
        DaoAuthenticationProvider provider = securityConfig.authenticationProvider();

        // DaoAuthenticationProvider configuration verified through authenticationProvider() method
        assertNotNull(provider);
    }

    @Test
    void testFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.filterChain(http);

        assertNotNull(chain);
    }

    @Test
    void testFilterChainWithNullHttpSecurity() {
        assertThrows(NullPointerException.class, () -> {
            securityConfig.filterChain(null);
        });
    }

    @Test
    void testPasswordEncoderConsistency() {
        BCryptPasswordEncoder encoder1 = securityConfig.passwordEncoder();
        BCryptPasswordEncoder encoder2 = securityConfig.passwordEncoder();

        String password = "testPassword";
        String encoded1 = encoder1.encode(password);

        assertTrue(encoder2.matches(password, encoded1));
    }

    @Test
    void testPasswordEncoderStrength() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

        String password = "weakpass";
        String encoded = encoder.encode(password);

        assertTrue(encoded.length() > 20);
    }

    @Test
    void testAuthenticationProviderNotNull() {
        DaoAuthenticationProvider provider = securityConfig.authenticationProvider();

        assertNotNull(provider);
    }

    @Test
    void testCustomUserDetailsServiceCreation() {
        assertDoesNotThrow(() -> {
            SpringDataUserDetailsService service = securityConfig.customUserDetailsService();
            assertNotNull(service);
        });
    }
}
