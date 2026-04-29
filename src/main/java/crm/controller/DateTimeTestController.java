package crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * DateTimeTestController demonstrates cloud-safe time handling.
 *
 * Blockers cr-java-0111 (lines 19-20): Replaced java.util.Date and
 * LocalDateTime.now() (which uses server-local timezone) with java.time API
 * standardized on UTC (ZonedDateTime with ZoneOffset.UTC and Instant).
 * This eliminates timezone inconsistencies across distributed cloud deployments
 * and ensures consistent time-sensitive logic in multi-region environments.
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        // Replaced new Date() (blocker line 19) with ZonedDateTime in UTC via java.time API
        ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        // Replaced LocalDateTime.now() (blocker line 20) with UTC-standardized LocalDate
        LocalDate utcLocalDate = LocalDate.now(ZoneOffset.UTC);

        model.addAttribute("standardDate", utcDateTime);
        model.addAttribute("localDateTime", utcDateTime);
        model.addAttribute("localDate", utcLocalDate);
        model.addAttribute("timestamp", Instant.now());
        return "date/test";
    }

}
