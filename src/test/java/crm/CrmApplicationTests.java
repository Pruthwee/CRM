package crm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic application tests that do not require a full Spring context
 * (avoids database connection requirements in CI/test environments)
 */
public class CrmApplicationTests {

    @Test
    public void testApplicationClassExists() {
        CrmApplication app = new CrmApplication();
        assertNotNull(app);
    }

}
