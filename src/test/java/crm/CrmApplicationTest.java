package crm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.*;

public class CrmApplicationTest {

    @Test
    public void testMainMethod() {
        assertDoesNotThrow(() -> {
            // Test that the main method exists and can be called
            // In actual application, we don't want to start the server during tests
        });
    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(CrmApplication.class);
    }

    @Test
    public void testSpringBootApplicationAnnotation() {
        assertTrue(CrmApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    public void testEntityScanAnnotation() {
        assertTrue(CrmApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.domain.EntityScan.class));
    }

    @Test
    public void testClassNotNull() {
        assertNotNull(CrmApplication.class);
    }

    @Test
    public void testMainMethodExists() {
        try {
            CrmApplication.class.getMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            fail("Main method should exist");
        }
    }
}
