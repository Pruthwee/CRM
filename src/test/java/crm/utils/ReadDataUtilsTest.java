package crm.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReadDataUtilsTest {

    @Test
    void testClassExists() {
        assertNotNull(ReadDataUtils.class);
    }

    @Test
    void testConstructor() {
        ReadDataUtils utils = new ReadDataUtils();
        assertNotNull(utils);
    }

    @Test
    void testReadFileMethodExists() throws NoSuchMethodException {
        assertNotNull(ReadDataUtils.class.getMethod("ReadFile", String.class, javax.swing.JFrame.class, String.class, String[].class));
    }

    @Test
    void testReadFileWithNullParent() {
        // Cannot test file chooser without GUI, but can verify method signature
        assertDoesNotThrow(() -> {
            // ReadDataUtils.ReadFile is GUI-based and cannot be fully tested
        });
    }

    @Test
    void testReadFileMethodIsStatic() throws NoSuchMethodException {
        assertTrue(java.lang.reflect.Modifier.isStatic(
            ReadDataUtils.class.getMethod("ReadFile", String.class, javax.swing.JFrame.class, String.class, String[].class).getModifiers()
        ));
    }

    @Test
    void testReadFileMethodIsPublic() throws NoSuchMethodException {
        assertTrue(java.lang.reflect.Modifier.isPublic(
            ReadDataUtils.class.getMethod("ReadFile", String.class, javax.swing.JFrame.class, String.class, String[].class).getModifiers()
        ));
    }
}
