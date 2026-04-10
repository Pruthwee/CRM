package crm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cloud-ready DateTime controller that standardizes on UTC timestamps.
 * Eliminates server-local timezone dependencies for distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
@Slf4j
public class DateTimeTestController {

    // Use UTC clock for all time operations
    private static final Clock UTC_CLOCK = Clock.systemUTC();
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // All timestamps in UTC for cloud consistency
        Instant currentInstant = Instant.now(UTC_CLOCK);
        ZonedDateTime utcDateTime = ZonedDateTime.now(UTC_CLOCK);
        
        // Store UTC timestamps
        model.addAttribute("timestamp", currentInstant);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("utcDateTimeFormatted", utcDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        model.addAttribute("epochMillis", currentInstant.toEpochMilli());
        model.addAttribute("isoTimestamp", currentInstant.toString());
        
        // Provide timezone information for display purposes only
        model.addAttribute("timezone", "UTC");
        model.addAttribute("zoneId", UTC_ZONE.getId());
        
        // Example: Convert to different timezones for display (business logic stays in UTC)
        ZonedDateTime eastCoastTime = utcDateTime.withZoneSameInstant(ZoneId.of("America/New_York"));
        ZonedDateTime pacificTime = utcDateTime.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
        ZonedDateTime europeTime = utcDateTime.withZoneSameInstant(ZoneId.of("Europe/London"));
        
        model.addAttribute("eastCoastTime", eastCoastTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        model.addAttribute("pacificTime", pacificTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        model.addAttribute("europeTime", europeTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        
        log.info("DateTime test accessed at UTC: {}", currentInstant);
        
        return "date/test";
    }

    /**
     * Get current UTC timestamp
     * @return Current Instant in UTC
     */
    public static Instant getCurrentUtcTimestamp() {
        return Instant.now(UTC_CLOCK);
    }

    /**
     * Convert UTC timestamp to specific timezone for display
     * @param utcInstant UTC timestamp
     * @param targetZoneId Target timezone
     * @return ZonedDateTime in target timezone
     */
    public static ZonedDateTime convertToTimezone(Instant utcInstant, String targetZoneId) {
        return utcInstant.atZone(ZoneId.of(targetZoneId));
    }

    /**
     * Parse ISO timestamp string to Instant
     * @param isoTimestamp ISO-8601 formatted timestamp
     * @return Instant object
     */
    public static Instant parseIsoTimestamp(String isoTimestamp) {
        return Instant.parse(isoTimestamp);
    }
}
