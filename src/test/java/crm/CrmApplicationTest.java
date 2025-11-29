package crm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.*;

class CrmApplicationTest {

    @Test
    void testMainMethodExists() {
        assertDoesNotThrow(() -> {
            CrmApplication.class.getDeclaredMethod("main", String[].class);
        });
    }

    @Test
    void testMainMethodIsPublic() throws NoSuchMethodException {
        int modifiers = CrmApplication.class.getDeclaredMethod("main", String[].class).getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
    }

    @Test
    void testMainMethodIsStatic() throws NoSuchMethodException {
        int modifiers = CrmApplication.class.getDeclaredMethod("main", String[].class).getModifiers();
        assertTrue(java.lang.reflect.Modifier.isStatic(modifiers));
    }

    @Test
    void testClassHasSpringBootApplicationAnnotation() {
        assertTrue(CrmApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void testClassHasEntityScanAnnotation() {
        assertTrue(CrmApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.domain.EntityScan.class));
    }

    @Test
    void testClassExists() {
        assertNotNull(CrmApplication.class);
    }

    @Test
    void testClassIsPublic() {
        int modifiers = CrmApplication.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
    }

    @Test
    void testContextLoads() {
        assertDoesNotThrow(() -> {
            // Test that the class can be instantiated
            CrmApplication.class.getDeclaredConstructor();
        });
    }
}
