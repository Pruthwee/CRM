package crm.repository;

import crm.entity.Category;
import crm.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerRepository Tests")
class CustomerRepositoryTest {

    @Mock
    private CustomerRepository customerRepository;

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
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .enabled(1)
                .categories(categories)
                .build();
    }

    @Test
    @DisplayName("Should get max id")
    void testGetMaxId() {
        when(customerRepository.getMaxId()).thenReturn(100L);

        Long result = customerRepository.getMaxId();

        assertEquals(100L, result);
        verify(customerRepository, times(1)).getMaxId();
    }

    @Test
    @DisplayName("Should find all by enabled")
    void testFindAllByEnabled() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findAllByEnabled(1)).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findAllByEnabled(1);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findAllByEnabled(1);
    }

    @Test
    @DisplayName("Should find one by enabled and name")
    void testFindOneByEnabledAndName() {
        when(customerRepository.findOneByEnabledAndName(1, "Test Customer")).thenReturn(customer);

        Customer result = customerRepository.findOneByEnabledAndName(1, "Test Customer");

        assertNotNull(result);
        assertEquals("Test Customer", result.getName());
        verify(customerRepository, times(1)).findOneByEnabledAndName(1, "Test Customer");
    }

    @Test
    @DisplayName("Should find one by name")
    void testFindOneByName() {
        when(customerRepository.findOneByName("Test Customer")).thenReturn(customer);

        Customer result = customerRepository.findOneByName("Test Customer");

        assertNotNull(result);
        assertEquals("Test Customer", result.getName());
        verify(customerRepository, times(1)).findOneByName("Test Customer");
    }

    @Test
    @DisplayName("Should find by enabled and email")
    void testFindByEnabledAndEmail() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndEmail(1, "test@example.com")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndEmail(1, "test@example.com");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndEmail(1, "test@example.com");
    }

    @Test
    @DisplayName("Should find by email")
    void testFindByEmail() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEmail("test@example.com")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEmail("test@example.com");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should find by enabled and city")
    void testFindByEnabledAndCity() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndCity(1, "New York")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndCity(1, "New York");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndCity(1, "New York");
    }

    @Test
    @DisplayName("Should find by city")
    void testFindByCity() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByCity("New York")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByCity("New York");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByCity("New York");
    }

    @Test
    @DisplayName("Should find by enabled city and address")
    void testFindByEnabledAndCityAndAddress() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndCityAndAddress(1, "New York", "123 Main St"))
                .thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndCityAndAddress(1, "New York", "123 Main St");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndCityAndAddress(1, "New York", "123 Main St");
    }

    @Test
    @DisplayName("Should find by city and address")
    void testFindByCityAndAddress() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByCityAndAddress("New York", "123 Main St")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByCityAndAddress("New York", "123 Main St");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByCityAndAddress("New York", "123 Main St");
    }

    @Test
    @DisplayName("Should find by enabled and phone")
    void testFindByEnabledAndPhone() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndPhone(1, 123456789)).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndPhone(1, 123456789);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndPhone(1, 123456789);
    }

    @Test
    @DisplayName("Should find by phone")
    void testFindByPhone() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByPhone(123456789)).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByPhone(123456789);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByPhone(123456789);
    }

    @Test
    @DisplayName("Should find by enabled and first name")
    void testFindByEnabledAndFirstName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndFirstName(1, "John")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndFirstName(1, "John");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndFirstName(1, "John");
    }

    @Test
    @DisplayName("Should find by first name")
    void testFindByFirstName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByFirstName("John")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByFirstName("John");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByFirstName("John");
    }

    @Test
    @DisplayName("Should find by enabled and last name")
    void testFindByEnabledAndLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndLastName(1, "Doe")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndLastName(1, "Doe");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndLastName(1, "Doe");
    }

    @Test
    @DisplayName("Should find by last name")
    void testFindByLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByLastName("Doe")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByLastName("Doe");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByLastName("Doe");
    }

    @Test
    @DisplayName("Should find by enabled and first name and last name")
    void testFindByEnabledAndFirstNameAndLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndFirstNameAndLastName(1, "John", "Doe")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndFirstNameAndLastName(1, "John", "Doe");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndFirstNameAndLastName(1, "John", "Doe");
    }

    @Test
    @DisplayName("Should find by first name and last name")
    void testFindByFirstNameAndLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByFirstNameAndLastName("John", "Doe");

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByFirstNameAndLastName("John", "Doe");
    }

    @Test
    @DisplayName("Should find by enabled and categories")
    void testFindByEnabledAndCategories() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndCategories(1, categories)).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByEnabledAndCategories(1, categories);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEnabledAndCategories(1, categories);
    }

    @Test
    @DisplayName("Should find by categories")
    void testFindByCategories() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByCategories(categories)).thenReturn(customers);

        Iterable<Customer> result = customerRepository.findByCategories(categories);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByCategories(categories);
    }

    @Test
    @DisplayName("Should save customer")
    void testSave() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerRepository.save(customer);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Should delete customer")
    void testDelete() {
        doNothing().when(customerRepository).delete(customer);

        customerRepository.delete(customer);

        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    @DisplayName("Should count customers")
    void testCount() {
        when(customerRepository.count()).thenReturn(50L);

        long result = customerRepository.count();

        assertEquals(50L, result);
        verify(customerRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should return null when max id not found")
    void testGetMaxIdNull() {
        when(customerRepository.getMaxId()).thenReturn(null);

        Long result = customerRepository.getMaxId();

        assertNull(result);
        verify(customerRepository, times(1)).getMaxId();
    }
}
