package crm.utils;

import crm.entity.Customer;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WriteCsvToResponseTest {

    @Test
    public void testWriteCustomersNotNull() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        List<Customer> customers = new ArrayList<>();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test");
        customer.setEmail("test@test.com");
        customers.add(customer);

        assertDoesNotThrow(() -> WriteCsvToResponse.writeCustomers(printWriter, customers));
    }

    @Test
    public void testWriteCustomersWithEmptyList() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        List<Customer> customers = new ArrayList<>();

        assertDoesNotThrow(() -> WriteCsvToResponse.writeCustomers(printWriter, customers));
    }

    @Test
    public void testWriteCustomer() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test");
        customer.setEmail("test@test.com");

        assertDoesNotThrow(() -> WriteCsvToResponse.writeCustomer(printWriter, customer));
    }
}
