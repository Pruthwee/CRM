package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
/**
 * Cloud-ready DateTime controller that uses UTC timestamps for consistency
 * across distributed cloud environments.
 */
        // Use UTC for all timestamps to ensure consistency across cloud regions
        Instant now = Instant.now();
        
        // All times in UTC to avoid timezone issues in distributed cloud environments
        model.addAttribute("timestamp", now);
        model.addAttribute("utcDateTime", ZonedDateTime.ofInstant(now, ZoneOffset.UTC));
        model.addAttribute("utcDate", LocalDate.now(ZoneOffset.UTC));
        model.addAttribute("epochMillis", now.toEpochMilli());
        
        // For display purposes, include ISO-8601 formatted string
        model.addAttribute("iso8601", now.toString());
        
    }

}
