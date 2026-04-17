package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Cloud-native date/time controller using java.time API.
 * All timestamps standardized to UTC to avoid timezone issues in distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    // Use Clock for testability and consistency across cloud instances
    private final Clock clock = Clock.systemUTC();

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use Instant for UTC timestamps - best practice for cloud environments
        Instant now = Instant.now(clock);
        
        // ZonedDateTime with explicit UTC timezone for clarity
        ZonedDateTime utcDateTime = ZonedDateTime.now(clock);
        
        // LocalDateTime in UTC context
        LocalDateTime localDateTime = LocalDateTime.now(clock);
        
        // LocalDate in UTC context
        LocalDate localDate = LocalDate.now(clock);
        
        // Add attributes with UTC-based time values
        model.addAttribute("timestamp", now);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("localDateTime", localDateTime);
        model.addAttribute("localDate", localDate);
        model.addAttribute("epochMilli", now.toEpochMilli());
        model.addAttribute("isoFormat", now.toString());
        
        return "date/test";
    }

}
