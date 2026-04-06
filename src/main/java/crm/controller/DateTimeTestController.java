package crm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * DateTime Test Controller - Cloud-ready version
 * Uses UTC timezone for consistency across distributed cloud environments
 * Replaced legacy Date with java.time API
 */
@Controller
@RequestMapping("/date")
@Slf4j
public class DateTimeTestController {

    @Value("${app.display.timezone:UTC}")
    private String displayTimezone;

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use UTC for all internal time operations (cloud best practice)
        Instant utcInstant = Instant.now();
        ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        LocalDateTime utcLocalDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDate utcLocalDate = LocalDate.now(ZoneId.of("UTC"));
        
        // For display purposes, convert to configured timezone if needed
        ZoneId displayZone = ZoneId.of(displayTimezone);
        ZonedDateTime displayDateTime = utcDateTime.withZoneSameInstant(displayZone);
        
        // Add attributes with UTC times (cloud-native approach)
        model.addAttribute("utcInstant", utcInstant);
        model.addAttribute("utcDateTime", utcDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        model.addAttribute("utcLocalDateTime", utcLocalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        model.addAttribute("utcLocalDate", utcLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Add display timezone information
        model.addAttribute("displayTimezone", displayTimezone);
        model.addAttribute("displayDateTime", displayDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        
        // Add epoch milliseconds for API compatibility
        model.addAttribute("epochMillis", utcInstant.toEpochMilli());
        
        log.info("DateTime test executed - UTC: {}, Display TZ: {}", utcDateTime, displayTimezone);
        
        return "date/test";
    }

    /**
     * Get current UTC timestamp as ISO-8601 string
     * @return ISO-8601 formatted UTC timestamp
     */
    public String getCurrentUtcTimestamp() {
        return Instant.now().toString();
    }

    /**
     * Convert UTC timestamp to specified timezone
     * @param utcInstant UTC instant
     * @param targetTimezone Target timezone ID
     * @return ZonedDateTime in target timezone
     */
    public ZonedDateTime convertToTimezone(Instant utcInstant, String targetTimezone) {
        return utcInstant.atZone(ZoneId.of(targetTimezone));
    }
}
