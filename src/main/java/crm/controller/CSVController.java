package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import crm.utils.WriteCsvToResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Cloud-ready CSV export controller.
 * Compatible with embedded servlet containers and cloud platforms.
 * Generates CSV responses in memory without file system dependencies.
 */
@RestController
public class CSVController {

    private static final Logger log = LoggerFactory.getLogger(CSVController.class);
    private CustomerService customerService;

    public CSVController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(value = "/customers", produces = "text/csv")
    public void findCustomers(HttpServletResponse httpServletResponse) throws IOException {
        try {
            List<Customer> customers = (List<Customer>) customerService.listAllCustomers();
            WriteCsvToResponse.writeCustomers(httpServletResponse.getWriter(), customers);
            log.info("Successfully exported {} customers to CSV", customers.size());
        } catch (Exception e) {
            log.error("Error exporting customers to CSV: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping(value = "/customers/{id}", produces = "text/csv")
    public void findCustomer(@PathVariable Long id, HttpServletResponse httpServletResponse) throws IOException {
        try {
            Customer customer = customerService.showCustomer(id);
            if (customer == null) {
                log.warn("Customer not found with id: {}", id);
                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            WriteCsvToResponse.writeCustomer(httpServletResponse.getWriter(), customer);
            log.info("Successfully exported customer {} to CSV", id);
        } catch (Exception e) {
            log.error("Error exporting customer {} to CSV: {}", id, e.getMessage(), e);
            throw e;
        }
    }

}
