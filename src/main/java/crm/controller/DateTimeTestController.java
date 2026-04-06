package crm.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cloud-ready date/time controller.
 * Uses UTC for all internal time operations and externalizes timezone configuration.
 * This ensures consistent behavior across distributed cloud environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    @Value("${app.timezone.display:UTC}")
    private String displayTimezone;

    @Value("${app.datetime.format:yyyy-MM-dd'T'HH:mm:ss'Z'}")
    private String dateTimeFormat;

    /**
     * Display date/time information using cloud-native patterns.
     * All times are stored/processed in UTC and converted for display based on configuration.
     */
    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Use Instant for UTC timestamps (cloud-native approach)
        Instant now = Instant.now();
        
        // Convert to configured timezone for display
        ZoneId displayZone = ZoneId.of(displayTimezone);
        ZonedDateTime zonedDateTime = now.atZone(displayZone);
        
        // Use java.time API instead of legacy Date class
        model.addAttribute("timestamp", now);
        model.addAttribute("utcDateTime", LocalDateTime.ofInstant(now, ZoneId.of("UTC")));
        model.addAttribute("localDateTime", zonedDateTime.toLocalDateTime());
        model.addAttribute("localDate", LocalDate.now(displayZone));
        model.addAttribute("zonedDateTime", zonedDateTime);
        model.addAttribute("timezone", displayTimezone);
        
        // Formatted timestamps for display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        model.addAttribute("formattedDateTime", zonedDateTime.format(formatter));
        
        // ISO-8601 format (recommended for APIs and cloud services)
        model.addAttribute("iso8601", now.toString());
        
        return "date/test";
    }

}
