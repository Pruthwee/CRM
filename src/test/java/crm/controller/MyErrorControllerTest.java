package crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MyErrorControllerTest {

    @InjectMocks
    private MyErrorController myErrorController;

    @Test
    void testError() {
        String result = myErrorController.error();
        assertNotNull(result);
        assertEquals("Error handling", result);
    }

    @Test
    void testErrorReturnsNonEmptyString() {
        String result = myErrorController.error();
        assertFalse(result.isEmpty());
    }

    @Test
    void testDefaultConstructor() {
        MyErrorController controller = new MyErrorController();
        assertNotNull(controller);
    }
}
