package crm.service;

import crm.entity.Category;
import crm.entity.Customer;
import crm.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceImpl Tests")
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

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
        Long result = customerService.getMaxId();
        assertEquals(100L, result);
        verify(customerRepository, times(1)).getMaxId();
    }

    @Test
    @DisplayName("Should list all customers")
    void testListAllCustomers() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.listAllCustomers();
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should show customer by id")
    void testShowCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Customer result = customerService.showCustomer(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should find enabled customers")
    void testFindAllByEnabledTrue() {
        when(customerRepository.findAllByEnabled(1)).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.findAllByEnabledTrue();
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findAllByEnabled(1);
    }

    @Test
    @DisplayName("Should find disabled customers")
    void testFindAllByEnabledFalse() {
        when(customerRepository.findAllByEnabled(0)).thenReturn(Arrays.asList());
        Iterable<Customer> result = customerService.findAllByEnabledFalse();
        assertNotNull(result);
        verify(customerRepository, times(1)).findAllByEnabled(0);
    }

    @Test
    @DisplayName("Should save customer")
    void testSaveCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        customerService.saveCustomer(customer);
        assertEquals(1, customer.getEnabled());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Should find customer by name")
    void testFindOneByName() {
        when(customerRepository.findOneByName("Test Customer")).thenReturn(customer);
        Customer result = customerService.findOneByName("Test Customer");
        assertNotNull(result);
        assertEquals("Test Customer", result.getName());
        verify(customerRepository, times(1)).findOneByName("Test Customer");
    }

    @Test
    @DisplayName("Should find by email")
    void testFindByEmail() {
        when(customerRepository.findByEmail("test@example.com")).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.findByEmail("test@example.com");
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should find by phone")
    void testFindByPhone() {
        when(customerRepository.findByPhone(123456789)).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.findByPhone(123456789);
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByPhone(123456789);
    }

    @Test
    @DisplayName("Should find by city")
    void testFindByCity() {
        when(customerRepository.findByCity("New York")).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.findByCity("New York");
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByCity("New York");
    }

    @Test
    @DisplayName("Should find by first name")
    void testFindByFirstName() {
        when(customerRepository.findByFirstName("John")).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.findByFirstName("John");
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByFirstName("John");
    }

    @Test
    @DisplayName("Should find by last name")
    void testFindByLastName() {
        when(customerRepository.findByLastName("Doe")).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.findByLastName("Doe");
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByLastName("Doe");
    }

    @Test
    @DisplayName("Should find by categories")
    void testFindByCategories() {
        when(customerRepository.findByCategories(categories)).thenReturn(Arrays.asList(customer));
        Iterable<Customer> result = customerService.findByCategories(categories);
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(customerRepository, times(1)).findByCategories(categories);
    }

    @Test
    @DisplayName("Should return null when customer not found")
    void testShowCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        Customer result = customerService.showCustomer(999L);
        assertNull(result);
        verify(customerRepository, times(1)).findById(999L);
    }
}
