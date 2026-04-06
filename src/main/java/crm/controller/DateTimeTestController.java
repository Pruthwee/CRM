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
 * Uses UTC timezone for consistency across distributed cloud environments.
 * Replaced legacy Date class with java.time API for better timezone handling.
 */
@Controller
@RequestMapping("/date")
@Slf4j
public class DateTimeTestController {

    @Value("${app.display.timezone:UTC}")
    private String displayTimezone;

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Always use UTC for internal operations in cloud environments
        Instant utcInstant = Instant.now();
        ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        LocalDateTime utcLocalDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDate utcLocalDate = LocalDate.now(ZoneId.of("UTC"));
        
        // For display purposes, convert to configured timezone
        ZoneId displayZone = ZoneId.of(displayTimezone);
        ZonedDateTime displayDateTime = utcDateTime.withZoneSameInstant(displayZone);
        
        // Add UTC times to model
        model.addAttribute("utcInstant", utcInstant);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("utcLocalDateTime", utcLocalDateTime);
        model.addAttribute("utcLocalDate", utcLocalDate);
        
        // Add display timezone information
        model.addAttribute("displayTimezone", displayTimezone);
        model.addAttribute("displayDateTime", displayDateTime);
        model.addAttribute("displayDateTimeFormatted", 
            displayDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        // Add ISO-8601 formatted strings for API compatibility
        model.addAttribute("iso8601Instant", utcInstant.toString());
        model.addAttribute("iso8601DateTime", utcDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        
        log.info("DateTime test executed - UTC: {}, Display TZ: {}", utcInstant, displayTimezone);
        
        return "date/test";
    }

    /**
     * Get current UTC timestamp as Instant
     * @return current UTC instant
     */
    public Instant getCurrentUtcInstant() {
        return Instant.now();
    }

    /**
     * Get current UTC date time
     * @return current UTC ZonedDateTime
     */
    public ZonedDateTime getCurrentUtcDateTime() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * Convert UTC time to display timezone
     * @param utcInstant the UTC instant
     * @return ZonedDateTime in display timezone
     */
    public ZonedDateTime convertToDisplayTimezone(Instant utcInstant) {
        ZoneId displayZone = ZoneId.of(displayTimezone);
        return utcInstant.atZone(displayZone);
    }
}
