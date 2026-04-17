package crm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cloud-ready DateTime controller that uses UTC timestamps
 * and avoids local timezone dependencies.
 * 
 * For scheduled operations, use Google Cloud Scheduler instead of java.util.Timer.
 * Cloud Scheduler configuration should be defined in infrastructure code (not here).
 */
@Controller
@RequestMapping("/date")
@Slf4j
public class DateTimeTestController {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Always use UTC for internal processing
        Instant currentInstant = Instant.now();
        ZonedDateTime utcDateTime = ZonedDateTime.now(UTC_ZONE);
        
        // Store UTC timestamps
        model.addAttribute("utcTimestamp", currentInstant);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("utcDateTimeFormatted", utcDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        
        // If timezone display is needed, get it from user preferences or request
        // Never rely on server's local timezone
        String userTimezone = System.getenv().getOrDefault("USER_TIMEZONE", "UTC");
        ZoneId userZoneId = ZoneId.of(userTimezone);
        ZonedDateTime userLocalTime = currentInstant.atZone(userZoneId);
        
        model.addAttribute("userTimezone", userTimezone);
        model.addAttribute("userLocalTime", userLocalTime);
        model.addAttribute("userLocalTimeFormatted", userLocalTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        
        // ISO-8601 formatted timestamp for APIs
        model.addAttribute("isoTimestamp", currentInstant.toString());
        
        log.info("DateTime test accessed - UTC: {}, User TZ: {}, User Local: {}", 
                currentInstant, userTimezone, userLocalTime);
        
        return "date/test";
    }
    
    /**
     * Example method showing how to handle timezone conversions.
     * Always store in UTC, convert to user timezone only for display.
     */
    public ZonedDateTime convertToUserTimezone(Instant utcInstant, String userTimezone) {
        try {
            ZoneId zoneId = ZoneId.of(userTimezone);
            return utcInstant.atZone(zoneId);
        } catch (Exception e) {
            log.warn("Invalid timezone: {}, falling back to UTC", userTimezone);
            return utcInstant.atZone(UTC_ZONE);
        }
    }
    
    /**
     * Note: For scheduled tasks, use Google Cloud Scheduler instead of java.util.Timer.
     * 
     * Cloud Scheduler setup (infrastructure code, not application code):
     * - Create Cloud Scheduler job via gcloud CLI or Terraform
     * - Configure HTTP target to call application endpoint
     * - Set schedule using cron expression
     * - Specify timezone in Cloud Scheduler (not in application)
     * 
     * Example gcloud command:
     * gcloud scheduler jobs create http my-scheduled-job \
     *   --schedule="0 2 * * *" \
     *   --uri="https://your-app.run.app/api/scheduled-task" \
     *   --http-method=POST \
     *   --time-zone="UTC"
     */

}
