package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import crm.utils.WriteCsvToResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Cloud-ready REST API Controller for CSV operations
 * - RESTful API design compatible with cloud load balancers and API gateways
 * - Proper HTTP status codes and error handling
 * - Stateless operations
 */
@RestController
@RequestMapping("/api/csv")
public class CSVController {

    private CustomerService customerService;

    public CSVController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Export all customers as CSV
     * GET /api/csv/customers
     */
    @GetMapping(value = "/customers", produces = "text/csv")
    public void findCustomers(HttpServletResponse httpServletResponse) throws IOException {
        List<Customer> customers = (List<Customer>) customerService.listAllCustomers();
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"customers.csv\"");
        WriteCsvToResponse.writeCustomers(httpServletResponse.getWriter(), customers);
    }

    /**
     * Export single customer as CSV
     * GET /api/csv/customers/{id}
     */
    @GetMapping(value = "/customers/{id}", produces = "text/csv")
    public void findCustomer(@PathVariable Long id, HttpServletResponse httpServletResponse) throws IOException {
        Customer customer = customerService.showCustomer(id);
        if (customer == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"customer-" + id + ".csv\"");
        WriteCsvToResponse.writeCustomer(httpServletResponse.getWriter(), customer);
    }

    /**
     * REST API endpoint to get customers as JSON
     * GET /api/csv/customers/json
     */
    @GetMapping(value = "/customers/json", produces = "application/json")
    public ResponseEntity<List<Customer>> getCustomersJson() {
        List<Customer> customers = (List<Customer>) customerService.listAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * REST API endpoint to get single customer as JSON
     * GET /api/csv/customers/{id}/json
     */
    @GetMapping(value = "/customers/{id}/json", produces = "application/json")
    public ResponseEntity<Customer> getCustomerJson(@PathVariable Long id) {
        Customer customer = customerService.showCustomer(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    /**
     * Health check endpoint for cloud load balancers
     * GET /api/csv/health
     */
    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"csv-export\"}");
    }

}
