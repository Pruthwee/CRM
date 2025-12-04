package crm.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class CSVTestTest {

    @Test
    public void testMainMethodExists() {
        assertDoesNotThrow(() -> {
            CSVTest.class.getDeclaredMethod("main", String[].class);
        });
    }

    @Test
    public void testCSVTestClassExists() {
        assertNotNull(CSVTest.class);
    }
}
