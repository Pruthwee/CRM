package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer Entity Tests")
class CustomerTest {

    private Customer customer;
    private Set<Category> categories;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        categories = new HashSet<>();
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Premium");
        categories.add(category1);
    }

    @Test
    @DisplayName("Should create customer with default constructor")
    void testDefaultConstructor() {
        assertNotNull(customer);
        assertNull(customer.getId());
        assertNull(customer.getName());
    }

    @Test
    @DisplayName("Should create customer with builder")
    void testBuilderConstructor() {
        Customer builtCustomer = Customer.builder()
                .id(1L)
                .name("ACME Corp")
                .email("contact@acme.com")
                .phone(1234567890)
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .enabled(1)
                .categories(categories)
                .build();

        assertNotNull(builtCustomer);
        assertEquals(1L, builtCustomer.getId());
        assertEquals("ACME Corp", builtCustomer.getName());
        assertEquals("contact@acme.com", builtCustomer.getEmail());
        assertEquals(1234567890, builtCustomer.getPhone());
        assertEquals("John", builtCustomer.getFirstName());
        assertEquals("Doe", builtCustomer.getLastName());
        assertEquals("New York", builtCustomer.getCity());
        assertEquals("123 Main St", builtCustomer.getAddress());
        assertEquals(1, builtCustomer.getEnabled());
        assertEquals(categories, builtCustomer.getCategories());
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testSettersAndGetters() {
        customer.setId(2L);
        customer.setName("Tech Solutions");
        customer.setEmail("info@techsolutions.com");
        customer.setPhone(987654321);
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setCity("San Francisco");
        customer.setAddress("456 Market St");
        customer.setEnabled(1);
        customer.setCategories(categories);

        assertEquals(2L, customer.getId());
        assertEquals("Tech Solutions", customer.getName());
        assertEquals("info@techsolutions.com", customer.getEmail());
        assertEquals(987654321, customer.getPhone());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("San Francisco", customer.getCity());
        assertEquals("456 Market St", customer.getAddress());
        assertEquals(1, customer.getEnabled());
        assertEquals(categories, customer.getCategories());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        customer.setId(null);
        customer.setName(null);
        customer.setEmail(null);
        customer.setFirstName(null);
        customer.setLastName(null);
        customer.setCity(null);
        customer.setAddress(null);
        customer.setCategories(null);

        assertNull(customer.getId());
        assertNull(customer.getName());
        assertNull(customer.getEmail());
        assertNull(customer.getFirstName());
        assertNull(customer.getLastName());
        assertNull(customer.getCity());
        assertNull(customer.getAddress());
        assertNull(customer.getCategories());
    }

    @Test
    @DisplayName("Should handle phone number zero")
    void testPhoneNumberZero() {
        customer.setPhone(0);
        assertEquals(0, customer.getPhone());
    }

    @Test
    @DisplayName("Should handle negative phone number")
    void testNegativePhoneNumber() {
        customer.setPhone(-123);
        assertEquals(-123, customer.getPhone());
    }

    @Test
    @DisplayName("Should handle large phone number")
    void testLargePhoneNumber() {
        customer.setPhone(999999999);
        assertEquals(999999999, customer.getPhone());
    }

    @Test
    @DisplayName("Should handle enabled flag")
    void testEnabledFlag() {
        customer.setEnabled(1);
        assertEquals(1, customer.getEnabled());

        customer.setEnabled(0);
        assertEquals(0, customer.getEnabled());
    }

    @Test
    @DisplayName("Should handle empty categories set")
    void testEmptyCategories() {
        Set<Category> emptyCategories = new HashSet<>();
        customer.setCategories(emptyCategories);
        assertNotNull(customer.getCategories());
        assertTrue(customer.getCategories().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple categories")
    void testMultipleCategories() {
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Standard");
        categories.add(category2);

        customer.setCategories(categories);

        assertEquals(2, customer.getCategories().size());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        customer.setName("");
        customer.setEmail("");
        customer.setFirstName("");
        customer.setLastName("");
        customer.setCity("");
        customer.setAddress("");

        assertEquals("", customer.getName());
        assertEquals("", customer.getEmail());
        assertEquals("", customer.getFirstName());
        assertEquals("", customer.getLastName());
        assertEquals("", customer.getCity());
        assertEquals("", customer.getAddress());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        Customer customer1 = Customer.builder()
                .id(1L)
                .name("Same Corp")
                .email("same@corp.com")
                .build();

        Customer customer2 = Customer.builder()
                .id(1L)
                .name("Same Corp")
                .email("same@corp.com")
                .build();

        assertEquals(customer1, customer2);
        assertEquals(customer1.hashCode(), customer2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        customer.setId(1L);
        customer.setName("Test Corp");
        String result = customer.toString();
        assertNotNull(result);
        assertTrue(result.contains("Customer"));
    }

    @Test
    @DisplayName("Should handle all args constructor")
    void testAllArgsConstructor() {
        Customer newCustomer = new Customer(
                3L,
                "Full Corp",
                "full@corp.com",
                555123456,
                categories,
                "Alice",
                "Johnson",
                "Boston",
                "789 Broadway",
                1
        );

        assertEquals(3L, newCustomer.getId());
        assertEquals("Full Corp", newCustomer.getName());
        assertEquals("full@corp.com", newCustomer.getEmail());
        assertEquals(555123456, newCustomer.getPhone());
        assertEquals(categories, newCustomer.getCategories());
        assertEquals("Alice", newCustomer.getFirstName());
        assertEquals("Johnson", newCustomer.getLastName());
        assertEquals("Boston", newCustomer.getCity());
        assertEquals("789 Broadway", newCustomer.getAddress());
        assertEquals(1, newCustomer.getEnabled());
    }

    @Test
    @DisplayName("Should handle no args constructor")
    void testNoArgsConstructor() {
        Customer newCustomer = new Customer();
        assertNotNull(newCustomer);
        assertNull(newCustomer.getId());
        assertNull(newCustomer.getName());
    }

    @Test
    @DisplayName("Should handle special characters in fields")
    void testSpecialCharacters() {
        customer.setName("Corp & Co.");
        customer.setAddress("123 Main St. #456");
        customer.setCity("São Paulo");

        assertEquals("Corp & Co.", customer.getName());
        assertEquals("123 Main St. #456", customer.getAddress());
        assertEquals("São Paulo", customer.getCity());
    }
}
