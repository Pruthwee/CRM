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
 * Cloud-ready DateTime controller that uses timezone-agnostic patterns.
 * All timestamps are in UTC to ensure consistency across distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    // Use UTC clock for cloud environments to avoid timezone inconsistencies
    private static final Clock UTC_CLOCK = Clock.systemUTC();
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use UTC-based timestamps for cloud consistency
        Instant utcInstant = Instant.now(UTC_CLOCK);
        ZonedDateTime utcZonedDateTime = ZonedDateTime.now(UTC_CLOCK);
        LocalDateTime utcLocalDateTime = LocalDateTime.now(UTC_CLOCK);
        LocalDate utcLocalDate = LocalDate.now(UTC_CLOCK);
        
        // Add UTC timestamps to model
        model.addAttribute("timestamp", utcInstant);
        model.addAttribute("zonedDateTime", utcZonedDateTime);
        model.addAttribute("localDateTime", utcLocalDateTime);
        model.addAttribute("localDate", utcLocalDate);
        model.addAttribute("timezone", "UTC");
        
        // Add ISO-8601 formatted strings for API compatibility
        model.addAttribute("timestampISO", utcInstant.toString());
        model.addAttribute("zonedDateTimeISO", utcZonedDateTime.toString());
        
        // Add epoch milliseconds for cross-platform compatibility
        model.addAttribute("epochMillis", utcInstant.toEpochMilli());
        
        return "date/test";
    }

    /**
     * Returns current UTC timestamp as Instant.
     * Use this method for all time-related operations in cloud environments.
     * 
     * @return Current UTC timestamp
     */
    public static Instant getCurrentUtcTimestamp() {
        return Instant.now(UTC_CLOCK);
    }

    /**
     * Returns current UTC ZonedDateTime.
     * 
     * @return Current UTC ZonedDateTime
     */
    public static ZonedDateTime getCurrentUtcZonedDateTime() {
        return ZonedDateTime.now(UTC_CLOCK);
    }

    /**
     * Converts an Instant to a specific timezone.
     * 
     * @param instant The instant to convert
     * @param zoneId The target timezone
     * @return ZonedDateTime in the specified timezone
     */
    public static ZonedDateTime convertToTimezone(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId);
    }

    /**
     * Parses an ISO-8601 timestamp string to Instant.
     * 
     * @param isoTimestamp ISO-8601 formatted timestamp
     * @return Instant representation
     */
    public static Instant parseIsoTimestamp(String isoTimestamp) {
        return Instant.parse(isoTimestamp);
    }
}
