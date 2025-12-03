package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CSVControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private CSVController csvController;

    private Customer testCustomer;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .build();

        when(httpServletResponse.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testConstructor() {
        CustomerService service = mock(CustomerService.class);
        CSVController controller = new CSVController(service);
        assertNotNull(controller);
    }

    @Test
    void testFindCustomers() throws IOException {
        when(customerService.listAllCustomers()).thenReturn(Arrays.asList(testCustomer));

        csvController.findCustomers(httpServletResponse);

        verify(customerService).listAllCustomers();
        verify(httpServletResponse).getWriter();
    }

    @Test
    void testFindCustomersEmpty() throws IOException {
        when(customerService.listAllCustomers()).thenReturn(Arrays.asList());

        csvController.findCustomers(httpServletResponse);

        verify(customerService).listAllCustomers();
    }

    @Test
    void testFindCustomer() throws IOException {
        when(customerService.showCustomer(1L)).thenReturn(testCustomer);

        csvController.findCustomer(1L, httpServletResponse);

        verify(customerService).showCustomer(1L);
        verify(httpServletResponse).getWriter();
    }

    @Test
    void testFindCustomerNotFound() throws IOException {
        when(customerService.showCustomer(999L)).thenReturn(null);

        csvController.findCustomer(999L, httpServletResponse);

        verify(customerService).showCustomer(999L);
    }

    @Test
    void testFindCustomersThrowsIOException() throws IOException {
        when(customerService.listAllCustomers()).thenReturn(Arrays.asList(testCustomer));
        when(httpServletResponse.getWriter()).thenThrow(new IOException("Test exception"));

        assertThrows(IOException.class, () -> {
            csvController.findCustomers(httpServletResponse);
        });
    }
}
