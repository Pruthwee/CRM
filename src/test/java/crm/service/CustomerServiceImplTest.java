package crm.service;

import crm.entity.Category;
import crm.entity.Customer;
import crm.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private Set<Category> categories;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Category category = new Category();
        category.setId(1L);
        category.setName("Premium");
        categories = new HashSet<>();
        categories.add(category);

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
                .categories(categories)
                .build();
    }

    @Test
    void testConstructor() {
        CustomerRepository repo = mock(CustomerRepository.class);
        CustomerServiceImpl service = new CustomerServiceImpl(repo);
        assertNotNull(service);
    }

    @Test
    void testGetMaxId() {
        when(customerRepository.getMaxId()).thenReturn(10L);

        Long result = customerService.getMaxId();

        assertEquals(10L, result);
        verify(customerRepository).getMaxId();
    }

    @Test
    void testGetMaxIdNull() {
        when(customerRepository.getMaxId()).thenReturn(null);

        Long result = customerService.getMaxId();

        assertNull(result);
    }

    @Test
    void testListAllCustomers() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.listAllCustomers();

        assertNotNull(result);
        verify(customerRepository).findAll();
    }

    @Test
    void testShowCustomer() {
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(testCustomer));

        Customer result = customerService.showCustomer(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testFindAllByEnabledTrue() {
        when(customerRepository.findAllByEnabled(1)).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findAllByEnabledTrue();

        assertNotNull(result);
        verify(customerRepository).findAllByEnabled(1);
    }

    @Test
    void testFindAllByEnabledFalse() {
        when(customerRepository.findAllByEnabled(0)).thenReturn(Arrays.asList());

        Iterable<Customer> result = customerService.findAllByEnabledFalse();

        assertNotNull(result);
        verify(customerRepository).findAllByEnabled(0);
    }

    @Test
    void testFindOneByEnabledTrueAndName() {
        when(customerRepository.findOneByEnabledAndName(1, "Test Customer")).thenReturn(testCustomer);

        Customer result = customerService.findOneByEnabledTrueAndName("Test Customer");

        assertNotNull(result);
        assertEquals("Test Customer", result.getName());
        verify(customerRepository).findOneByEnabledAndName(1, "Test Customer");
    }

    @Test
    void testFindOneByEnabledFalseAndName() {
        when(customerRepository.findOneByEnabledAndName(0, "Test Customer")).thenReturn(null);

        Customer result = customerService.findOneByEnabledFalseAndName("Test Customer");

        assertNull(result);
        verify(customerRepository).findOneByEnabledAndName(0, "Test Customer");
    }

    @Test
    void testFindOneByName() {
        when(customerRepository.findOneByName("Test Customer")).thenReturn(testCustomer);

        Customer result = customerService.findOneByName("Test Customer");

        assertNotNull(result);
        verify(customerRepository).findOneByName("Test Customer");
    }

    @Test
    void testFindByEnabledTrueAndEmail() {
        when(customerRepository.findByEnabledAndEmail(1, "test@example.com")).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndEmail("test@example.com");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(1, "test@example.com");
    }

    @Test
    void testFindByEnabledFalseAndEmail() {
        when(customerRepository.findByEnabledAndEmail(0, "test@example.com")).thenReturn(Arrays.asList());

        Iterable<Customer> result = customerService.findByEnabledFalseAndEmail("test@example.com");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(0, "test@example.com");
    }

    @Test
    void testFindByEmail() {
        when(customerRepository.findByEmail("test@example.com")).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEmail("test@example.com");

        assertNotNull(result);
        verify(customerRepository).findByEmail("test@example.com");
    }

    @Test
    void testFindByEnabledTrueAndPhone() {
        when(customerRepository.findByEnabledAndPhone(1, 123456789)).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndPhone(123456789);

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndPhone(1, 123456789);
    }

    @Test
    void testFindByPhone() {
        when(customerRepository.findByPhone(123456789)).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByPhone(123456789);

        assertNotNull(result);
        verify(customerRepository).findByPhone(123456789);
    }

    @Test
    void testFindByEnabledTrueAndCategories() {
        when(customerRepository.findByEnabledAndCategories(1, categories)).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndCategories(categories);

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCategories(1, categories);
    }

    @Test
    void testFindByEnabledTrueAndFirstName() {
        when(customerRepository.findByEnabledAndFirstName(1, "John")).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstName("John");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstName(1, "John");
    }

    @Test
    void testFindByEnabledTrueAndLastName() {
        when(customerRepository.findByEnabledAndLastName(1, "Doe")).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndLastName("Doe");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndLastName(1, "Doe");
    }

    @Test
    void testFindByEnabledTrueAndFirstNameAndLastName() {
        when(customerRepository.findByEnabledAndFirstNameAndLastName(1, "John", "Doe")).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstNameAndLastName("John", "Doe");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstNameAndLastName(1, "John", "Doe");
    }

    @Test
    void testFindByEnabledTrueAndCity() {
        when(customerRepository.findByEnabledAndCity(1, "New York")).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndCity("New York");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCity(1, "New York");
    }

    @Test
    void testFindByEnabledTrueAndCityAndAddress() {
        when(customerRepository.findByEnabledAndCityAndAddress(1, "New York", "123 Main St")).thenReturn(Arrays.asList(testCustomer));

        Iterable<Customer> result = customerService.findByEnabledTrueAndCityAndAddress("New York", "123 Main St");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCityAndAddress(1, "New York", "123 Main St");
    }

    @Test
    void testSaveCustomer() {
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        customerService.saveCustomer(testCustomer);

        assertEquals(1, testCustomer.getEnabled());
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void testSaveCustomerSetsEnabled() {
        testCustomer.setEnabled(0);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        customerService.saveCustomer(testCustomer);

        assertEquals(1, testCustomer.getEnabled());
    }
}
