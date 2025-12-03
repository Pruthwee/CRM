package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;
    private Set<Category> categories;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Premium");

        categories = new HashSet<>();
        categories.add(category);

        customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .categories(categories)
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .enabled(1)
                .build();
    }

    @Test
    void testConstructor() {
        assertNotNull(customer);
    }

    @Test
    void testBuilder() {
        Customer built = Customer.builder()
                .name("Builder Customer")
                .email("builder@test.com")
                .build();

        assertNotNull(built);
        assertEquals("Builder Customer", built.getName());
    }

    @Test
    void testId() {
        assertEquals(1L, customer.getId());
        customer.setId(2L);
        assertEquals(2L, customer.getId());
    }

    @Test
    void testName() {
        assertEquals("Test Customer", customer.getName());
        customer.setName("Updated Customer");
        assertEquals("Updated Customer", customer.getName());
    }

    @Test
    void testEmail() {
        assertEquals("test@example.com", customer.getEmail());
        customer.setEmail("new@example.com");
        assertEquals("new@example.com", customer.getEmail());
    }

    @Test
    void testPhone() {
        assertEquals(123456789, customer.getPhone());
        customer.setPhone(987654321);
        assertEquals(987654321, customer.getPhone());
    }

    @Test
    void testCategories() {
        assertNotNull(customer.getCategories());
        assertEquals(1, customer.getCategories().size());
    }

    @Test
    void testFirstName() {
        assertEquals("John", customer.getFirstName());
        customer.setFirstName("Jane");
        assertEquals("Jane", customer.getFirstName());
    }

    @Test
    void testLastName() {
        assertEquals("Doe", customer.getLastName());
        customer.setLastName("Smith");
        assertEquals("Smith", customer.getLastName());
    }

    @Test
    void testCity() {
        assertEquals("New York", customer.getCity());
        customer.setCity("Boston");
        assertEquals("Boston", customer.getCity());
    }

    @Test
    void testAddress() {
        assertEquals("123 Main St", customer.getAddress());
        customer.setAddress("456 Oak Ave");
        assertEquals("456 Oak Ave", customer.getAddress());
    }

    @Test
    void testEnabled() {
        assertEquals(1, customer.getEnabled());
        customer.setEnabled(0);
        assertEquals(0, customer.getEnabled());
    }

    @Test
    void testNullEmail() {
        customer.setEmail(null);
        assertNull(customer.getEmail());
    }

    @Test
    void testNullCategories() {
        customer.setCategories(null);
        assertNull(customer.getCategories());
    }

    @Test
    void testNoArgsConstructor() {
        Customer empty = new Customer();
        assertNotNull(empty);
    }

    @Test
    void testAllArgsConstructor() {
        Customer full = new Customer(1L, "Name", "email@test.com", 123456,
                categories, "First", "Last", "City", "Address", 1);
        assertNotNull(full);
        assertEquals("Name", full.getName());
    }
}
