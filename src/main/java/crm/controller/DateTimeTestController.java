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
 * Cloud-ready DateTime controller that uses UTC timezone and explicit Clock for distributed environments.
 * Replaces server-local timezone dependencies with UTC-based time handling.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    @Value("${app.timezone:UTC}")
    private String applicationTimezone;

    /**
     * Returns a Clock instance configured for the application timezone.
     * Defaults to UTC for cloud environments to ensure consistency across regions.
     * 
     * @return Clock instance
     */
    private Clock getApplicationClock() {
        ZoneId zoneId = ZoneId.of(applicationTimezone);
        return Clock.system(zoneId);
    }

    /**
     * Test endpoint that demonstrates cloud-ready date/time handling.
     * All timestamps are generated using UTC timezone to ensure consistency
     * across distributed cloud deployments.
     * 
     * @param model Spring MVC model
     * @return View name
     */
    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        Clock clock = getApplicationClock();
        
        // Use Instant for UTC timestamps (recommended for cloud environments)
        Instant utcTimestamp = Instant.now(clock);
        
        // Use ZonedDateTime for timezone-aware timestamps
        ZonedDateTime zonedDateTime = ZonedDateTime.now(clock);
        
        // Use LocalDateTime with explicit clock
        LocalDateTime localDateTime = LocalDateTime.now(clock);
        
        // Use LocalDate with explicit clock
        LocalDate localDate = LocalDate.now(clock);
        
        // Add timezone information to model
        model.addAttribute("timezone", applicationTimezone);
        model.addAttribute("utcTimestamp", utcTimestamp);
        model.addAttribute("zonedDateTime", zonedDateTime);
        model.addAttribute("localDateTime", localDateTime);
        model.addAttribute("localDate", localDate);
        
        // Add ISO-8601 formatted timestamps for API compatibility
        model.addAttribute("iso8601Timestamp", utcTimestamp.toString());
        model.addAttribute("epochMillis", utcTimestamp.toEpochMilli());
        
        return "date/test";
    }

    /**
     * Returns the current UTC timestamp as an Instant.
     * This method is useful for scheduled tasks and time-based operations
     * that need to be consistent across distributed cloud environments.
     * 
     * @return Current UTC timestamp
     */
    public Instant getCurrentUtcTimestamp() {
        return Instant.now(Clock.systemUTC());
    }

    /**
     * Converts a timestamp to a specific timezone.
     * 
     * @param instant The timestamp to convert
     * @param targetTimezone The target timezone (e.g., "America/New_York")
     * @return ZonedDateTime in the target timezone
     */
    public ZonedDateTime convertToTimezone(Instant instant, String targetTimezone) {
        ZoneId zoneId = ZoneId.of(targetTimezone);
        return instant.atZone(zoneId);
    }
}
