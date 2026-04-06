package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import crm.utils.WriteCsvToResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * REST Controller for CSV operations.
 * Cloud-ready implementation that exports customer data as CSV.
 * 
 * For CSV import functionality in cloud environments:
 * - Implement file upload endpoint using MultipartFile
 * - Store uploaded files in AWS S3 or process them in-memory
 * - Use streaming for large files to avoid memory issues
 */
@RestController
public class CSVController {

    private CustomerService customerService;

    public CSVController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(value = "/customers", produces = "text/csv")
    public void findCustomers(HttpServletResponse httpServletResponse) throws IOException {
        List<Customer> customers = (List<Customer>) customerService.listAllCustomers();
        WriteCsvToResponse.writeCustomers(httpServletResponse.getWriter(), customers);
    }

    @GetMapping(value = "/customers/{id}", produces = "text/csv")
    public void findCustomer(@PathVariable Long id, HttpServletResponse httpServletResponse) throws IOException {
        Customer customer = customerService.showCustomer(id);
        WriteCsvToResponse.writeCustomer(httpServletResponse.getWriter(), customer);
    }

    // Note: CSV import functionality using desktop file chooser has been removed
    // as it's not compatible with cloud environments.
    // 
    // To implement CSV import in cloud:
    // 1. Create a file upload endpoint:
    //    @PostMapping("/customers/import")
    //    public ResponseEntity<?> importCustomers(@RequestParam("file") MultipartFile file)
    // 
    // 2. Process the uploaded file in-memory or save to S3
    // 
    // 3. Parse CSV and save customers to database
}
