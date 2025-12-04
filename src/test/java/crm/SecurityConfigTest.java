package crm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new SecurityConfig();
    }

    @Test
    @DisplayName("Should create SecurityConfig instance")
    void testInstantiation() {
        assertNotNull(config);
    }

    @Test
    @DisplayName("Should have Configuration annotation")
    void testConfigurationAnnotation() {
        assertTrue(SecurityConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class));
    }
}
