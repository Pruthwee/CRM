package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    private Customer customer;

    @BeforeEach
    public void setUp() {
        customer = new Customer();
    }

    @Test
    public void testCustomerConstructor() {
        assertNotNull(customer);
    }

    @Test
    public void testCustomerBuilderPattern() {
        Set<Category> categories = new HashSet<>();
        Customer builtCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .categories(categories)
                .firstName("Test")
                .lastName("Customer")
                .city("Test City")
                .address("123 Test St")
                .enabled(1)
                .build();
        assertNotNull(builtCustomer);
        assertEquals("Test Customer", builtCustomer.getName());
        assertEquals("test@example.com", builtCustomer.getEmail());
    }

    @Test
    public void testAllArgsConstructor() {
        Set<Category> categories = new HashSet<>();
        Customer fullCustomer = new Customer(1L, "Test", "test@example.com", 123456789, categories, "Test", "Customer", "City", "Address", 1);
        assertNotNull(fullCustomer);
        assertEquals("Test", fullCustomer.getName());
    }

    @Test
    public void testSetAndGetId() {
        customer.setId(1L);
        assertEquals(1L, customer.getId());
    }

    @Test
    public void testSetAndGetName() {
        customer.setName("Test");
        assertEquals("Test", customer.getName());
    }

    @Test
    public void testSetAndGetEmail() {
        customer.setEmail("test@example.com");
        assertEquals("test@example.com", customer.getEmail());
    }

    @Test
    public void testSetAndGetPhone() {
        customer.setPhone(123456789);
        assertEquals(123456789, customer.getPhone());
    }

    @Test
    public void testSetAndGetCategories() {
        Set<Category> categories = new HashSet<>();
        Category category = new Category();
        categories.add(category);
        customer.setCategories(categories);
        assertEquals(categories, customer.getCategories());
    }

    @Test
    public void testSetAndGetFirstName() {
        customer.setFirstName("Test");
        assertEquals("Test", customer.getFirstName());
    }

    @Test
    public void testSetAndGetLastName() {
        customer.setLastName("Customer");
        assertEquals("Customer", customer.getLastName());
    }

    @Test
    public void testSetAndGetCity() {
        customer.setCity("Test City");
        assertEquals("Test City", customer.getCity());
    }

    @Test
    public void testSetAndGetAddress() {
        customer.setAddress("123 Test St");
        assertEquals("123 Test St", customer.getAddress());
    }

    @Test
    public void testSetAndGetEnabled() {
        customer.setEnabled(1);
        assertEquals(1, customer.getEnabled());
    }

    @Test
    public void testCustomerWithNullName() {
        customer.setName(null);
        assertNull(customer.getName());
    }

    @Test
    public void testCustomerWithNullEmail() {
        customer.setEmail(null);
        assertNull(customer.getEmail());
    }

    @Test
    public void testCustomerWithZeroPhone() {
        customer.setPhone(0);
        assertEquals(0, customer.getPhone());
    }

    @Test
    public void testCustomerWithEmptyCategories() {
        Set<Category> emptyCategories = new HashSet<>();
        customer.setCategories(emptyCategories);
        assertTrue(customer.getCategories().isEmpty());
    }
}
