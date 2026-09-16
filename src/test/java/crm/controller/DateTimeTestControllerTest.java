package crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateTimeTestControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private DateTimeTestController dateTimeTestController;

    @Test
    void testDateTimeTest_returnsCorrectView() {
        String view = dateTimeTestController.dateTimeTest(model);
        assertEquals("date/test", view);
    }

    @Test
    void testDateTimeTest_addsStandardDate() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("standardDate"), any(Date.class));
    }

    @Test
    void testDateTimeTest_addsLocalDateTime() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("localDateTime"), any(LocalDateTime.class));
    }

    @Test
    void testDateTimeTest_addsLocalDate() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("localDate"), any(LocalDate.class));
    }

    @Test
    void testDateTimeTest_addsTimestamp() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("timestamp"), any(Instant.class));
    }

    @Test
    void testDateTimeTest_addsAllFourAttributes() {
        dateTimeTestController.dateTimeTest(model);
        verify(model, times(4)).addAttribute(anyString(), any());
    }
}
