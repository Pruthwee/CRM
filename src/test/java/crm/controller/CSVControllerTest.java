package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CSVControllerTest {

    private CSVController csvController;
    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        customerService = mock(CustomerService.class);
        csvController = new CSVController(customerService);
    }

    @Test
    public void testCSVControllerConstructor() {
        assertNotNull(csvController);
    }

    @Test
    public void testFindCustomers() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        List<Customer> customers = new ArrayList<>();

        when(response.getWriter()).thenReturn(writer);
        when(customerService.listAllCustomers()).thenReturn(customers);

        assertDoesNotThrow(() -> csvController.findCustomers(response));
        verify(customerService).listAllCustomers();
    }

    @Test
    public void testFindCustomer() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        Customer customer = new Customer();

        when(response.getWriter()).thenReturn(writer);
        when(customerService.showCustomer(1L)).thenReturn(customer);

        assertDoesNotThrow(() -> csvController.findCustomer(1L, response));
        verify(customerService).showCustomer(1L);
    }
}
