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
    void testError_returnsErrorHandlingString() {
        String result = myErrorController.error();
        assertEquals("Error handling", result);
    }

    @Test
    void testError_returnsNonNullString() {
        String result = myErrorController.error();
        assertNotNull(result);
    }

    @Test
    void testError_returnsNonEmptyString() {
        String result = myErrorController.error();
        assertFalse(result.isEmpty());
    }

    @Test
    void testError_multipleCallsReturnSameValue() {
        String result1 = myErrorController.error();
        String result2 = myErrorController.error();
        assertEquals(result1, result2);
    }
}
