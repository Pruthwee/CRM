package crm.utils;

import crm.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WriteCsvToResponseTest {

    private PrintWriter printWriter;
    private StringWriter stringWriter;
    private List<Customer> customers;
    private Customer customer;

    @BeforeEach
    public void setUp() {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .enabled(1)
                .build();

        customers = new ArrayList<>();
        customers.add(customer);
    }

    @Test
    public void testWriteCustomersMethodExists() {
        assertNotNull(WriteCsvToResponse.class);
        assertDoesNotThrow(() -> {
            WriteCsvToResponse.class.getMethod("writeCustomers", PrintWriter.class, List.class);
        });
    }

    @Test
    public void testWriteCustomerMethodExists() {
        assertDoesNotThrow(() -> {
            WriteCsvToResponse.class.getMethod("writeCustomer", PrintWriter.class, Customer.class);
        });
    }

    @Test
    public void testWriteCustomersWithEmptyList() {
        List<Customer> emptyList = new ArrayList<>();
        assertDoesNotThrow(() -> {
            WriteCsvToResponse.writeCustomers(printWriter, emptyList);
        });
    }

    @Test
    public void testWriteCustomersStaticMethod() throws NoSuchMethodException {
        java.lang.reflect.Method method = WriteCsvToResponse.class.getMethod("writeCustomers", PrintWriter.class, List.class);
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
    }

    @Test
    public void testWriteCustomerStaticMethod() throws NoSuchMethodException {
        java.lang.reflect.Method method = WriteCsvToResponse.class.getMethod("writeCustomer", PrintWriter.class, Customer.class);
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
    }

    @Test
    public void testWriteCustomersPublicMethod() throws NoSuchMethodException {
        java.lang.reflect.Method method = WriteCsvToResponse.class.getMethod("writeCustomers", PrintWriter.class, List.class);
        assertTrue(java.lang.reflect.Modifier.isPublic(method.getModifiers()));
    }

    @Test
    public void testWriteCustomerPublicMethod() throws NoSuchMethodException {
        java.lang.reflect.Method method = WriteCsvToResponse.class.getMethod("writeCustomer", PrintWriter.class, Customer.class);
        assertTrue(java.lang.reflect.Modifier.isPublic(method.getModifiers()));
    }

    @Test
    public void testClassNotNull() {
        assertNotNull(WriteCsvToResponse.class);
    }

    @Test
    public void testWriteCustomersWithNullPrintWriter() {
        assertThrows(NullPointerException.class, () -> {
            WriteCsvToResponse.writeCustomers(null, customers);
        });
    }

    @Test
    public void testWriteCustomerWithNullPrintWriter() {
        assertThrows(NullPointerException.class, () -> {
            WriteCsvToResponse.writeCustomer(null, customer);
        });
    }
}
