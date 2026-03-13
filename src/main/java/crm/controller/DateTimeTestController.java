import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloud-Ready DateTime Test Controller with RESTful API support
 */
@RequestMapping("/date")
public class DateTimeTestController {

    @GetMapping("/test")
    public String dateTimeTest(Model model) {
        model.addAttribute("standardDate", new Date());
        model.addAttribute("localDateTime", LocalDateTime.now());
    /**
     * REST API: Get current date/time information
     * Cloud-ready endpoint for API gateways and load balancers
     */
    @GetMapping("/api/datetime")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDateTimeApi() {
        Map<String, Object> dateTimeInfo = new HashMap<>();
        dateTimeInfo.put("standardDate", new Date());
        dateTimeInfo.put("localDateTime", LocalDateTime.now());
        dateTimeInfo.put("localDate", LocalDate.now());
        dateTimeInfo.put("timestamp", Instant.now());
        dateTimeInfo.put("serverTime", System.currentTimeMillis());
        return ResponseEntity.ok(dateTimeInfo);
    }


}
