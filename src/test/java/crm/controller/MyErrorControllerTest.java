package crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class MyErrorControllerTest {

    private MyErrorController myErrorController;

    @BeforeEach
    public void setUp() {
        myErrorController = new MyErrorController();
    }

    @Test
    public void testMyErrorControllerConstructor() {
        assertNotNull(myErrorController);
    }

    @Test
    public void testError() {
        String result = myErrorController.error();
        assertEquals("Error handling", result);
    }

    @Test
    public void testErrorNotNull() {
        String result = myErrorController.error();
        assertNotNull(result);
    }
}
