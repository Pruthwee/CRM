package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
    }

    @Test
    void testCustomerBuilder() {
        Set<Category> categories = new HashSet<>();
        Customer built = Customer.builder()
                .id(1L)
                .name("Test Company")
                .email("test@example.com")
                .phone(123456789)
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .enabled(1)
                .categories(categories)
                .build();

        assertNotNull(built);
        assertEquals(1L, built.getId());
        assertEquals("Test Company", built.getName());
        assertEquals("test@example.com", built.getEmail());
        assertEquals(123456789, built.getPhone());
        assertEquals("John", built.getFirstName());
        assertEquals("Doe", built.getLastName());
        assertEquals("New York", built.getCity());
        assertEquals("123 Main St", built.getAddress());
        assertEquals(1, built.getEnabled());
    }

    @Test
    void testGettersAndSetters() {
        customer.setId(1L);
        customer.setName("Test Company");
        customer.setEmail("test@example.com");
        customer.setPhone(123456789);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCity("New York");
        customer.setAddress("123 Main St");
        customer.setEnabled(1);

        assertEquals(1L, customer.getId());
        assertEquals("Test Company", customer.getName());
        assertEquals("test@example.com", customer.getEmail());
        assertEquals(123456789, customer.getPhone());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("New York", customer.getCity());
        assertEquals("123 Main St", customer.getAddress());
        assertEquals(1, customer.getEnabled());
    }

    @Test
    void testSetCategories() {
        Set<Category> categories = new HashSet<>();
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        categories.add(category);

        customer.setCategories(categories);

        assertNotNull(customer.getCategories());
        assertEquals(1, customer.getCategories().size());
    }

    @Test
    void testEmailValidation() {
        customer.setEmail("test@example.com");
        assertTrue(customer.getEmail().contains("@"));
    }

    @Test
    void testEnabledStatus() {
        customer.setEnabled(1);
        assertEquals(1, customer.getEnabled());

        customer.setEnabled(0);
        assertEquals(0, customer.getEnabled());
    }

    @Test
    void testPhoneNumber() {
        customer.setPhone(1234567890);
        assertTrue(customer.getPhone() > 0);
    }

    @Test
    void testNullValues() {
        customer.setName(null);
        customer.setEmail(null);
        customer.setFirstName(null);
        customer.setLastName(null);
        customer.setCity(null);
        customer.setAddress(null);

        assertNull(customer.getName());
        assertNull(customer.getEmail());
        assertNull(customer.getFirstName());
        assertNull(customer.getLastName());
        assertNull(customer.getCity());
        assertNull(customer.getAddress());
    }

    @Test
    void testCustomerEntity() {
        assertTrue(customer.getClass().isAnnotationPresent(jakarta.persistence.Entity.class));
    }

    @Test
    void testCustomerHasIdAnnotation() throws NoSuchFieldException {
        assertTrue(Customer.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.Id.class));
    }

    @Test
    void testAllArgsConstructor() {
        Set<Category> categories = new HashSet<>();
        Customer customer = new Customer(1L, "Test", "test@example.com", 123456, categories,
                                        "John", "Doe", "NYC", "123 St", 1);
        assertNotNull(customer);
        assertEquals(1L, customer.getId());
    }

    @Test
    void testNoArgsConstructor() {
        Customer customer = new Customer();
        assertNotNull(customer);
    }
}
