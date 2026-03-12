package crm.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloud-ready Error Controller
 * - REST API compatible error handling
 * - Works with cloud load balancers and API gateways
 */
@RestController
public class MyErrorController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH, produces = "application/json")
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            errorResponse.put("status", statusCode);
            errorResponse.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
        } else {
            errorResponse.put("status", 500);
            errorResponse.put("error", "Internal Server Error");
        }
        
        errorResponse.put("message", message != null ? message.toString() : "An error occurred");
        errorResponse.put("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        
        return ResponseEntity.status((Integer) errorResponse.get("status")).body(errorResponse);
    }

    public String getErrorPath() {
        return PATH;
    }

}
