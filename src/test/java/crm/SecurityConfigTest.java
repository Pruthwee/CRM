package crm;

import crm.service.SpringDataUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void testPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder);
    }

    @Test
    void testPasswordEncoderEncodesPasswords() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testPasswordEncoderMatchesReturnsFalseForWrongPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = encoder.encode(rawPassword);

        assertFalse(encoder.matches("wrongPassword", encodedPassword));
    }

    @Test
    void testCustomUserDetailsService() {
        SpringDataUserDetailsService service = securityConfig.customUserDetailsService();
        assertNotNull(service);
    }

    @Test
    void testClassIsConfigurationAnnotated() {
        assertTrue(securityConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }

    @Test
    void testClassHasEnableWebSecurityAnnotation() {
        assertTrue(securityConfig.getClass().isAnnotationPresent(org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class));
    }

    @Test
    void testClassHasEnableMethodSecurityAnnotation() {
        assertTrue(securityConfig.getClass().isAnnotationPresent(org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class));
    }
}
