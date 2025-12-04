package crm.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadDataUtilsTest {

    @Test
    public void testReadDataUtilsClassExists() {
        assertNotNull(ReadDataUtils.class);
    }

    @Test
    public void testReadFileMethodExists() throws Exception {
        assertNotNull(ReadDataUtils.class.getDeclaredMethod("ReadFile", String.class, javax.swing.JFrame.class, String.class, String[].class));
    }
}
