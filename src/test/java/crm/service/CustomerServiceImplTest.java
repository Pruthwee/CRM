package crm.service;

import crm.entity.Category;
import crm.entity.Customer;
import crm.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private Set<Category> testCategories;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        testCategories = new HashSet<>();
        testCategories.add(category);

        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .firstName("John")
                .lastName("Doe")
                .city("Test City")
                .address("123 Test St")
                .enabled(1)
                .categories(testCategories)
                .build();
    }

    @Test
    void testConstructor() {
        CustomerServiceImpl service = new CustomerServiceImpl(customerRepository);
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
    void testListAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        Iterable<Customer> result = customerService.listAllCustomers();

        assertNotNull(result);
        verify(customerRepository).findAll();
    }

    @Test
    void testShowCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        Customer result = customerService.showCustomer(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testShowCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        Customer result = customerService.showCustomer(999L);

        assertNull(result);
        verify(customerRepository).findById(999L);
    }

    @Test
    void testFindAllByEnabledTrue() {
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        when(customerRepository.findAllByEnabled(1)).thenReturn(customers);

        Iterable<Customer> result = customerService.findAllByEnabledTrue();

        assertNotNull(result);
        verify(customerRepository).findAllByEnabled(1);
    }

    @Test
    void testFindAllByEnabledFalse() {
        when(customerRepository.findAllByEnabled(0)).thenReturn(new ArrayList<>());

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
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        when(customerRepository.findByEnabledAndEmail(1, "test@example.com")).thenReturn(customers);

        Iterable<Customer> result = customerService.findByEnabledTrueAndEmail("test@example.com");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(1, "test@example.com");
    }

    @Test
    void testFindByEnabledFalseAndEmail() {
        when(customerRepository.findByEnabledAndEmail(0, "test@example.com")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndEmail("test@example.com");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(0, "test@example.com");
    }

    @Test
    void testFindByEmail() {
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        when(customerRepository.findByEmail("test@example.com")).thenReturn(customers);

        Iterable<Customer> result = customerService.findByEmail("test@example.com");

        assertNotNull(result);
        verify(customerRepository).findByEmail("test@example.com");
    }

    @Test
    void testFindByEnabledTrueAndPhone() {
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        when(customerRepository.findByEnabledAndPhone(1, 123456789)).thenReturn(customers);

        Iterable<Customer> result = customerService.findByEnabledTrueAndPhone(123456789);

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndPhone(1, 123456789);
    }

    @Test
    void testFindByEnabledFalseAndPhone() {
        when(customerRepository.findByEnabledAndPhone(0, 123456789)).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndPhone(123456789);

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndPhone(0, 123456789);
    }

    @Test
    void testFindByPhone() {
        List<Customer> customers = new ArrayList<>();
        when(customerRepository.findByPhone(123456789)).thenReturn(customers);

        Iterable<Customer> result = customerService.findByPhone(123456789);

        assertNotNull(result);
        verify(customerRepository).findByPhone(123456789);
    }

    @Test
    void testFindByEnabledTrueAndCategories() {
        List<Customer> customers = new ArrayList<>();
        when(customerRepository.findByEnabledAndCategories(1, testCategories)).thenReturn(customers);

        Iterable<Customer> result = customerService.findByEnabledTrueAndCategories(testCategories);

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCategories(1, testCategories);
    }

    @Test
    void testFindByEnabledFalseAndCategories() {
        when(customerRepository.findByEnabledAndCategories(0, testCategories)).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndCategories(testCategories);

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCategories(0, testCategories);
    }

    @Test
    void testFindByCategories() {
        when(customerRepository.findByCategories(testCategories)).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByCategories(testCategories);

        assertNotNull(result);
        verify(customerRepository).findByCategories(testCategories);
    }

    @Test
    void testFindByEnabledTrueAndFirstName() {
        when(customerRepository.findByEnabledAndFirstName(1, "John")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstName("John");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstName(1, "John");
    }

    @Test
    void testFindByEnabledFalseAndFirstName() {
        when(customerRepository.findByEnabledAndFirstName(0, "John")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndFirstName("John");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstName(0, "John");
    }

    @Test
    void testFindByFirstName() {
        when(customerRepository.findByFirstName("John")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByFirstName("John");

        assertNotNull(result);
        verify(customerRepository).findByFirstName("John");
    }

    @Test
    void testFindByEnabledTrueAndLastName() {
        when(customerRepository.findByEnabledAndLastName(1, "Doe")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledTrueAndLastName("Doe");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndLastName(1, "Doe");
    }

    @Test
    void testFindByEnabledFalseAndLastName() {
        when(customerRepository.findByEnabledAndLastName(0, "Doe")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndLastName("Doe");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndLastName(0, "Doe");
    }

    @Test
    void testFindByLastName() {
        when(customerRepository.findByLastName("Doe")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByLastName("Doe");

        assertNotNull(result);
        verify(customerRepository).findByLastName("Doe");
    }

    @Test
    void testFindByEnabledTrueAndFirstNameAndLastName() {
        when(customerRepository.findByEnabledAndFirstNameAndLastName(1, "John", "Doe")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstNameAndLastName("John", "Doe");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstNameAndLastName(1, "John", "Doe");
    }

    @Test
    void testFindByEnabledFalseAndFirstNameAndLastName() {
        when(customerRepository.findByEnabledAndFirstNameAndLastName(0, "John", "Doe")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndFirstNameAndLastName("John", "Doe");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstNameAndLastName(0, "John", "Doe");
    }

    @Test
    void testFindByFirstNameAndLastName() {
        when(customerRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByFirstNameAndLastName("John", "Doe");

        assertNotNull(result);
        verify(customerRepository).findByFirstNameAndLastName("John", "Doe");
    }

    @Test
    void testFindByEnabledTrueAndCity() {
        when(customerRepository.findByEnabledAndCity(1, "Test City")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledTrueAndCity("Test City");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCity(1, "Test City");
    }

    @Test
    void testFindByEnabledFalseAndCity() {
        when(customerRepository.findByEnabledAndCity(0, "Test City")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndCity("Test City");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCity(0, "Test City");
    }

    @Test
    void testFindByCity() {
        when(customerRepository.findByCity("Test City")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByCity("Test City");

        assertNotNull(result);
        verify(customerRepository).findByCity("Test City");
    }

    @Test
    void testFindByEnabledTrueAndCityAndAddress() {
        when(customerRepository.findByEnabledAndCityAndAddress(1, "Test City", "123 Test St")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledTrueAndCityAndAddress("Test City", "123 Test St");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCityAndAddress(1, "Test City", "123 Test St");
    }

    @Test
    void testFindByEnabledFalseAndCityAndAddress() {
        when(customerRepository.findByEnabledAndCityAndAddress(0, "Test City", "123 Test St")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByEnabledFalseAndCityAndAddress("Test City", "123 Test St");

        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCityAndAddress(0, "Test City", "123 Test St");
    }

    @Test
    void testFindByCityAndAddress() {
        when(customerRepository.findByCityAndAddress("Test City", "123 Test St")).thenReturn(new ArrayList<>());

        Iterable<Customer> result = customerService.findByCityAndAddress("Test City", "123 Test St");

        assertNotNull(result);
        verify(customerRepository).findByCityAndAddress("Test City", "123 Test St");
    }

    @Test
    void testSaveCustomer() {
        customerService.saveCustomer(testCustomer);

        assertEquals(1, testCustomer.getEnabled());
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void testSaveCustomerSetsEnabledToOne() {
        testCustomer.setEnabled(0);

        customerService.saveCustomer(testCustomer);

        assertEquals(1, testCustomer.getEnabled());
        verify(customerRepository).save(testCustomer);
    }
}
