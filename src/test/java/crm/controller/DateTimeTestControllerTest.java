package crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DateTimeTestControllerTest {

    private DateTimeTestController dateTimeTestController;

    @BeforeEach
    public void setUp() {
        dateTimeTestController = new DateTimeTestController();
    }

    @Test
    public void testDateTimeTestControllerConstructor() {
        assertNotNull(dateTimeTestController);
    }

    @Test
    public void testDateTimeTest() {
        Model model = mock(Model.class);
        String viewName = dateTimeTestController.dateTimeTest(model);
        assertEquals("date/test", viewName);
        verify(model, times(4)).addAttribute(anyString(), any());
    }

    @Test
    public void testDateTimeTestAddsAttributes() {
        Model model = mock(Model.class);
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("standardDate"), any());
        verify(model).addAttribute(eq("localDateTime"), any());
        verify(model).addAttribute(eq("localDate"), any());
        verify(model).addAttribute(eq("timestamp"), any());
    }
}
