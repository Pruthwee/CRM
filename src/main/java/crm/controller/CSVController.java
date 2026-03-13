package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import crm.utils.WriteCsvToResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Cloud-Ready CSV REST Controller
 * - RESTful API design for cloud load balancers and API gateways
 * - Stateless operations for horizontal scaling
 * - Supports content negotiation for cloud environments
 */
@RestController
@RequestMapping("/api/csv")
public class CSVController {

    private final CustomerService customerService;

    public CSVController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * RESTful endpoint to export all customers as CSV
     * Cloud-ready: Returns CSV data as response entity
     */
    @GetMapping(value = "/customers", produces = "text/csv")
    public void findCustomers(HttpServletResponse httpServletResponse) throws IOException {
        List<Customer> customers = (List<Customer>) customerService.listAllCustomers();
        httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers.csv");
        WriteCsvToResponse.writeCustomers(httpServletResponse.getWriter(), customers);
    }

    /**
     * RESTful endpoint to export single customer as CSV
     * Cloud-ready: Returns CSV data as response entity
     */
    @GetMapping(value = "/customers/{id}", produces = "text/csv")
    public void findCustomer(@PathVariable Long id, HttpServletResponse httpServletResponse) throws IOException {
        Customer customer = customerService.showCustomer(id);
        httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customer-" + id + ".csv");
        WriteCsvToResponse.writeCustomer(httpServletResponse.getWriter(), customer);
    }
    
    /**
     * Alternative RESTful endpoint returning CSV as ResponseEntity
     * Better for cloud environments with API gateways
     */
    @GetMapping(value = "/customers/export", produces = "text/csv")
    public ResponseEntity<String> exportCustomers() {
        try {
            List<Customer> customers = (List<Customer>) customerService.listAllCustomers();
            StringWriter writer = new StringWriter();
            WriteCsvToResponse.writeCustomers(writer, customers);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "customers.csv");
            
            return new ResponseEntity<>(writer.toString(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Alternative RESTful endpoint for single customer export
     * Better for cloud environments with API gateways
     */
    @GetMapping(value = "/customers/{id}/export", produces = "text/csv")
    public ResponseEntity<String> exportCustomer(@PathVariable Long id) {
        try {
            Customer customer = customerService.showCustomer(id);
            StringWriter writer = new StringWriter();
            WriteCsvToResponse.writeCustomer(writer, customer);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "customer-" + id + ".csv");
            
            return new ResponseEntity<>(writer.toString(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
