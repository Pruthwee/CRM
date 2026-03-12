package crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloud-ready DateTime Test Controller
 * - REST API endpoints for cloud load balancers
 * - Stateless operations
 */
@Controller
@RequestMapping("/date")
public class DateTimeTestController {

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        model.addAttribute("standardDate", new Date());
        model.addAttribute("localDateTime", LocalDateTime.now());
        model.addAttribute("localDate", LocalDate.now());
        model.addAttribute("timestamp", Instant.now());
        return "date/test";
    }

    /**
     * REST API endpoint to get current date/time information
     * GET /date/api/current
     * Cloud-ready: Stateless operation
     */
    @GetMapping("/api/current")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentDateTime() {
        Map<String, Object> dateTimeInfo = new HashMap<>();
        dateTimeInfo.put("standardDate", new Date());
        dateTimeInfo.put("localDateTime", LocalDateTime.now());
        dateTimeInfo.put("localDate", LocalDate.now());
        dateTimeInfo.put("timestamp", Instant.now());
        dateTimeInfo.put("epochMillis", System.currentTimeMillis());
        return ResponseEntity.ok(dateTimeInfo);
    }

}
