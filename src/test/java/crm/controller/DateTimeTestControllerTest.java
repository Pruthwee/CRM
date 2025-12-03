package crm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DateTimeTestControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private DateTimeTestController dateTimeTestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructor() {
        DateTimeTestController controller = new DateTimeTestController();
        assertNotNull(controller);
    }

    @Test
    void testDateTimeTest() {
        String result = dateTimeTestController.dateTimeTest(model);

        assertEquals("date/test", result);
        verify(model, times(4)).addAttribute(anyString(), any());
    }

    @Test
    void testDateTimeTestAttributesAdded() {
        dateTimeTestController.dateTimeTest(model);

        verify(model).addAttribute(eq("standardDate"), any());
        verify(model).addAttribute(eq("localDateTime"), any());
        verify(model).addAttribute(eq("localDate"), any());
        verify(model).addAttribute(eq("timestamp"), any());
    }
}
