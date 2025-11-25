package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    private Customer customer;
    private Set<Category> categories;

    @BeforeEach
    public void setUp() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Technology");

        categories = new HashSet<>();
        categories.add(category1);

        customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("customer@test.com")
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
    public void testCustomerBuilder() {
        assertNotNull(customer);
        assertEquals(1L, customer.getId());
        assertEquals("Test Customer", customer.getName());
        assertEquals("customer@test.com", customer.getEmail());
        assertEquals(123456789, customer.getPhone());
        assertEquals(categories, customer.getCategories());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("New York", customer.getCity());
        assertEquals("123 Main St", customer.getAddress());
        assertEquals(1, customer.getEnabled());
    }

    @Test
    public void testCustomerNoArgsConstructor() {
        Customer emptyCustomer = new Customer();
        assertNotNull(emptyCustomer);
    }

    @Test
    public void testCustomerAllArgsConstructor() {
        Customer newCustomer = new Customer(
                2L, "New Customer", "new@test.com", 987654321,
                categories, "Jane", "Smith", "Boston", "456 Oak Ave", 1
        );
        assertNotNull(newCustomer);
        assertEquals(2L, newCustomer.getId());
        assertEquals("New Customer", newCustomer.getName());
    }

    @Test
    public void testSettersAndGetters() {
        customer.setName("Updated Customer");
        assertEquals("Updated Customer", customer.getName());

        customer.setEmail("updated@test.com");
        assertEquals("updated@test.com", customer.getEmail());

        customer.setPhone(111222333);
        assertEquals(111222333, customer.getPhone());

        customer.setFirstName("Jane");
        assertEquals("Jane", customer.getFirstName());

        customer.setLastName("Smith");
        assertEquals("Smith", customer.getLastName());

        customer.setCity("Los Angeles");
        assertEquals("Los Angeles", customer.getCity());

        customer.setAddress("789 Pine Rd");
        assertEquals("789 Pine Rd", customer.getAddress());

        customer.setEnabled(0);
        assertEquals(0, customer.getEnabled());
    }

    @Test
    public void testCustomerWithNullValues() {
        Customer nullCustomer = new Customer();
        nullCustomer.setId(null);
        nullCustomer.setName(null);
        nullCustomer.setEmail(null);
        nullCustomer.setCategories(null);
        nullCustomer.setFirstName(null);
        nullCustomer.setLastName(null);
        nullCustomer.setCity(null);
        nullCustomer.setAddress(null);

        assertNull(nullCustomer.getId());
        assertNull(nullCustomer.getName());
        assertNull(nullCustomer.getEmail());
        assertNull(nullCustomer.getCategories());
        assertNull(nullCustomer.getFirstName());
        assertNull(nullCustomer.getLastName());
        assertNull(nullCustomer.getCity());
        assertNull(nullCustomer.getAddress());
    }

    @Test
    public void testCustomerCategories() {
        assertNotNull(customer.getCategories());
        assertEquals(1, customer.getCategories().size());
    }

    @Test
    public void testCustomerAddCategory() {
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Finance");

        customer.getCategories().add(category2);
        assertEquals(2, customer.getCategories().size());
    }

    @Test
    public void testCustomerEnabled() {
        customer.setEnabled(1);
        assertEquals(1, customer.getEnabled());

        customer.setEnabled(0);
        assertEquals(0, customer.getEnabled());
    }

    @Test
    public void testCustomerPhoneNumber() {
        customer.setPhone(123456789);
        assertTrue(customer.getPhone() > 0);
    }

    @Test
    public void testCustomerEmailFormat() {
        customer.setEmail("valid@email.com");
        assertTrue(customer.getEmail().contains("@"));
    }

    @Test
    public void testCustomerNameMinLength() {
        customer.setName("AB");
        assertTrue(customer.getName().length() >= 2);
    }
}
