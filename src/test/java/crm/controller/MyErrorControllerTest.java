package crm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class MyErrorControllerTest {

    @InjectMocks
    private MyErrorController myErrorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructor() {
        MyErrorController controller = new MyErrorController();
        assertNotNull(controller);
    }

    @Test
    void testError() {
        String result = myErrorController.error();

        assertEquals("Error handling", result);
    }

    @Test
    void testErrorNotNull() {
        String result = myErrorController.error();

        assertNotNull(result);
    }

    @Test
    void testErrorReturnsExpectedMessage() {
        String result = myErrorController.error();

        assertTrue(result.contains("Error"));
    }
}
