package crm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CrmApplicationTest {

    @Test
    public void testCrmApplicationClassExists() {
        assertNotNull(CrmApplication.class);
    }

    @Test
    public void testMainMethodExists() {
        assertDoesNotThrow(() -> {
            CrmApplication.class.getDeclaredMethod("main", String[].class);
        });
    }

    @Test
    public void testCrmApplicationConstructor() {
        assertDoesNotThrow(() -> {
            CrmApplication application = new CrmApplication();
            assertNotNull(application);
        });
    }
}
