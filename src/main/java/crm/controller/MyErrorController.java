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
 * Cloud-Ready Error Controller
 * - RESTful API design for cloud load balancers and API gateways
 * - Provides structured error responses for cloud monitoring
 * - Compatible with Spring Boot 2.x ErrorController interface
 */
@RestController
public class MyErrorController implements ErrorController {

    private static final String PATH = "/error";

    /**
     * Handle errors with structured response for cloud environments
     * Returns JSON error information for better cloud monitoring
     */
    @RequestMapping(value = PATH)
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            errorResponse.put("status", statusCode);
            errorResponse.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
        } else {
            errorResponse.put("status", 500);
            errorResponse.put("error", "Internal Server Error");
        }
        
        if (message != null) {
            errorResponse.put("message", message.toString());
        } else {
            errorResponse.put("message", "An error occurred");
        }
        
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        HttpStatus httpStatus = status != null ? 
            HttpStatus.valueOf(Integer.valueOf(status.toString())) : 
            HttpStatus.INTERNAL_SERVER_ERROR;
            
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    /**
     * Returns the error path for Spring Boot
     * Required by ErrorController interface
     */
    public String getErrorPath() {
        return PATH;
    }
}
