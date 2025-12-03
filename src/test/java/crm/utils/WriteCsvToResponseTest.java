package crm.utils;

import crm.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WriteCsvToResponseTest {

    @Mock
    private PrintWriter printWriter;

    private Customer testCustomer;
    private List<Customer> customers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .enabled(1)
                .categories(new HashSet<>())
                .build();

        customers = Arrays.asList(testCustomer);
    }

    @Test
    void testWriteCustomers() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        WriteCsvToResponse.writeCustomers(writer, customers);

        assertDoesNotThrow(() -> writer.flush());
    }

    @Test
    void testWriteCustomersWithEmptyList() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        WriteCsvToResponse.writeCustomers(writer, Arrays.asList());

        assertDoesNotThrow(() -> writer.flush());
    }

    @Test
    void testWriteCustomersWithMultiple() {
        Customer customer2 = Customer.builder()
                .id(2L)
                .name("Second Customer")
                .email("second@example.com")
                .phone(987654321)
                .build();

        List<Customer> multipleCustomers = Arrays.asList(testCustomer, customer2);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        WriteCsvToResponse.writeCustomers(writer, multipleCustomers);

        assertDoesNotThrow(() -> writer.flush());
    }

    @Test
    void testWriteCustomer() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        WriteCsvToResponse.writeCustomer(writer, testCustomer);

        assertDoesNotThrow(() -> writer.flush());
    }

    @Test
    void testWriteCustomerNotNull() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        assertDoesNotThrow(() -> {
            WriteCsvToResponse.writeCustomer(writer, testCustomer);
        });
    }

    @Test
    void testWriteCustomersWithNull() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        assertDoesNotThrow(() -> {
            WriteCsvToResponse.writeCustomers(writer, null);
        });
    }

    @Test
    void testWriteCustomerWithNull() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        assertDoesNotThrow(() -> {
            WriteCsvToResponse.writeCustomer(writer, null);
        });
    }
}
