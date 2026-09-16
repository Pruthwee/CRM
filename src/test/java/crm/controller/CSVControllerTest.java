package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CSVControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private CSVController csvController;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L).name("TestCo").email("test@test.com")
                .phone(123456789).firstName("John").lastName("Doe")
                .city("NYC").address("123 Main").enabled(1).build();
    }

    @Test
    void testFindCustomers() throws IOException {
        List<Customer> customers = Arrays.asList(customer);
        when(customerService.listAllCustomers()).thenReturn(customers);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(httpServletResponse.getWriter()).thenReturn(pw);
        csvController.findCustomers(httpServletResponse);
        verify(customerService).listAllCustomers();
        verify(httpServletResponse).getWriter();
    }

    @Test
    void testFindCustomer() throws IOException {
        when(customerService.showCustomer(1L)).thenReturn(customer);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(httpServletResponse.getWriter()).thenReturn(pw);
        csvController.findCustomer(1L, httpServletResponse);
        verify(customerService).showCustomer(1L);
        verify(httpServletResponse).getWriter();
    }

    @Test
    void testFindCustomers_EmptyList() throws IOException {
        List<Customer> customers = Collections.emptyList();
        when(customerService.listAllCustomers()).thenReturn(customers);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(httpServletResponse.getWriter()).thenReturn(pw);
        csvController.findCustomers(httpServletResponse);
        verify(customerService).listAllCustomers();
    }

    @Test
    void testConstructorWithService() {
        CSVController controller = new CSVController(customerService);
        assertNotNull(controller);
    }
}
