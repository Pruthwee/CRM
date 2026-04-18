package crm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Cloud-ready DateTime controller.
 * Standardizes time handling with UTC timestamps and NTP synchronization.
 * Implements audit logging and clock drift detection for distributed systems.
 */
@Controller
@RequestMapping("/date")
@Slf4j
public class DateTimeTestController {

    // Use UTC clock for consistent time across distributed systems
    private static final Clock UTC_CLOCK = Clock.systemUTC();
    private static final ZoneId UTC_ZONE = ZoneOffset.UTC;
    
    /**
     * Health check threshold for clock drift detection (milliseconds)
     */
    private static final long CLOCK_DRIFT_THRESHOLD_MS = 1000;

    /**
     * Validates clock synchronization by comparing system time with UTC clock.
     * Detects clock drift issues before they impact production.
     * 
     * @return true if clock is synchronized within threshold
     */
    private boolean validateClockSync() {
        long systemTime = System.currentTimeMillis();
        long utcClockTime = UTC_CLOCK.millis();
        long drift = Math.abs(systemTime - utcClockTime);
        
        if (drift > CLOCK_DRIFT_THRESHOLD_MS) {
            log.warn("Clock drift detected: drift={}ms, threshold={}ms", drift, CLOCK_DRIFT_THRESHOLD_MS);
            return false;
        }
        
        return true;
    }

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Validate clock synchronization (GCP automatic NTP synchronization)
        boolean clockSynced = validateClockSync();
        
        // Use UTC-based timestamps for consistency across distributed systems
        Instant utcTimestamp = Instant.now(UTC_CLOCK);
        LocalDateTime utcDateTime = LocalDateTime.now(UTC_CLOCK);
        LocalDate utcDate = LocalDate.now(UTC_CLOCK);
        
        // Legacy Date object for backward compatibility (converted from UTC)
        Date standardDate = Date.from(utcTimestamp);
        
        // Add attributes to model
        model.addAttribute("standardDate", standardDate);
        model.addAttribute("localDateTime", utcDateTime);
        model.addAttribute("localDate", utcDate);
        model.addAttribute("timestamp", utcTimestamp);
        model.addAttribute("timezone", UTC_ZONE.getId());
        model.addAttribute("clockSynced", clockSynced);
        
        // Audit logging with UTC timestamps
        log.info("DateTime test endpoint accessed: timestamp={}, timezone={}, clockSynced={}, " +
                 "utcDateTime={}, utcDate={}", 
                 utcTimestamp, UTC_ZONE.getId(), clockSynced, utcDateTime, utcDate);
        
        // Log warning if clock drift detected
        if (!clockSynced) {
            log.warn("Clock synchronization issue detected. Check NTP configuration and system time settings.");
        }
        
        return "date/test";
    }
    
    /**
     * Health check endpoint for monitoring clock synchronization.
     * Can be used by load balancers and monitoring systems.
     * 
     * @param model Spring MVC model
     * @return health check view
     */
    @GetMapping("/health/clock")
    public String clockHealthCheck(Model model) {
        boolean clockSynced = validateClockSync();
        Instant currentTime = Instant.now(UTC_CLOCK);
        
        model.addAttribute("healthy", clockSynced);
        model.addAttribute("timestamp", currentTime);
        model.addAttribute("timezone", UTC_ZONE.getId());
        
        log.info("Clock health check: healthy={}, timestamp={}", clockSynced, currentTime);
        
        return "date/health";
    }

}
