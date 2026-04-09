import java.time.ZoneOffset;
import java.time.ZonedDateTime;
/**
 * Cloud-ready DateTime controller that uses UTC timezone for all operations.
 * Eliminates server-local timezone dependencies for distributed cloud environments.
 */
        // Use UTC timezone for all date/time operations to ensure consistency across cloud regions
        Instant now = Instant.now();
        ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        
        model.addAttribute("timestamp", now);
        model.addAttribute("utcDateTime", utcDateTime);
        model.addAttribute("utcDate", LocalDate.now(ZoneOffset.UTC));
        model.addAttribute("utcLocalDateTime", LocalDateTime.now(ZoneOffset.UTC));
        model.addAttribute("epochMillis", now.toEpochMilli());
        
        return "date/test";
    }

}
