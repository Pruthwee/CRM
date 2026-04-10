package crm.controller;

import org.springframework.beans.factory.annotation.Value;
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
 * Cloud-ready DateTime controller with timezone-aware handling.
 * Uses UTC as the standard timezone and allows configuration via environment variables.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    @Value("${app.timezone:UTC}")
    private String applicationTimezone;

    /**
     * Returns current date/time in a cloud-native, timezone-aware manner.
     * Uses UTC as the base timezone and allows configuration via environment variables.
     */
    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use UTC clock for consistent behavior across distributed cloud environments
        Clock utcClock = Clock.systemUTC();
        ZoneId zoneId = ZoneId.of(applicationTimezone);

        // Instant represents a point in time in UTC (timezone-agnostic)
        Instant timestamp = Instant.now(utcClock);
        
        // ZonedDateTime for timezone-aware operations
        ZonedDateTime zonedDateTime = ZonedDateTime.now(utcClock).withZoneSameInstant(zoneId);
        
        // LocalDateTime in the configured timezone
        LocalDateTime localDateTime = LocalDateTime.now(Clock.system(zoneId));
        
        // LocalDate in the configured timezone
        LocalDate localDate = LocalDate.now(Clock.system(zoneId));

        // Add timezone-aware attributes to the model
        model.addAttribute("timestamp", timestamp);
        model.addAttribute("timestampUtc", timestamp.toString());
        model.addAttribute("zonedDateTime", zonedDateTime);
        model.addAttribute("localDateTime", localDateTime);
        model.addAttribute("localDate", localDate);
        model.addAttribute("timezone", applicationTimezone);
        model.addAttribute("zoneId", zoneId.toString());

        return "date/test";
    }
}
