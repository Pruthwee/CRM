package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Cloud-native date/time controller using java.time API with UTC standardization.
 * Eliminates timezone inconsistencies in distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    // Use UTC clock for consistent time across all cloud regions and containers
    private final Clock utcClock = Clock.systemUTC();

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use java.time API with UTC standardization for cloud environments
        Instant currentInstant = Instant.now(utcClock);
        ZonedDateTime utcDateTime = ZonedDateTime.now(utcClock);
        LocalDateTime localDateTime = LocalDateTime.now(utcClock);
        LocalDate localDate = LocalDate.now(utcClock);
        
        // Add UTC-based timestamps to model
        model.addAttribute("timestamp", currentInstant);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("localDateTime", localDateTime);
        model.addAttribute("localDate", localDate);
        
        // Add timezone information for display
        model.addAttribute("timezone", ZoneId.of("UTC"));
        model.addAttribute("epochMillis", currentInstant.toEpochMilli());
        
        return "date/test";
    }
}
