package crm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CrmApplication Tests")
class CrmApplicationTest {

    @Test
    @DisplayName("Should test main method exists")
    void testMainMethod() {
        assertDoesNotThrow(() -> CrmApplication.class.getMethod("main", String[].class));
    }

    @Test
    @DisplayName("Should have Spring Boot Application annotation")
    void testSpringBootApplication() {
        assertTrue(CrmApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }
}
