package crm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebAppConfig Tests")
class WebAppConfigTest {

    private WebAppConfig config;

    @BeforeEach
    void setUp() {
        config = new WebAppConfig();
    }

    @Test
    @DisplayName("Should create WebAppConfig instance")
    void testInstantiation() {
        assertNotNull(config);
    }

    @Test
    @DisplayName("Should create WebAppConfig successfully")
    void testWebAppConfigCreation() {
        assertNotNull(config);
        assertTrue(config instanceof WebAppConfig);
    }
}
