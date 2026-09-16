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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        customer1 = Customer.builder()
                .id(1L).name("Acme").email("acme@test.com")
                .phone(123456789).firstName("John").lastName("Doe")
                .city("NYC").address("123 Main").enabled(1).build();

        customer2 = Customer.builder()
                .id(2L).name("Beta").email("beta@test.com")
                .phone(987654321).firstName("Jane").lastName("Smith")
                .city("LA").address("456 Oak").enabled(0).build();
    }

    @Test
    void testGetMaxId() {
        when(customerRepository.getMaxId()).thenReturn(5L);
        Long result = customerService.getMaxId();
        assertEquals(5L, result);
        verify(customerRepository).getMaxId();
    }

    @Test
    void testListAllCustomers() {
        List<Customer> customers = Arrays.asList(customer1, customer2);
        when(customerRepository.findAll()).thenReturn(customers);
        Iterable<Customer> result = customerService.listAllCustomers();
        assertNotNull(result);
        verify(customerRepository).findAll();
    }

    @Test
    void testShowCustomer_Found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer1));
        Customer result = customerService.showCustomer(1L);
        assertNotNull(result);
        assertEquals("Acme", result.getName());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testShowCustomer_NotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        Customer result = customerService.showCustomer(99L);
        assertNull(result);
        verify(customerRepository).findById(99L);
    }

    @Test
    void testFindAllByEnabledTrue() {
        List<Customer> enabled = Collections.singletonList(customer1);
        when(customerRepository.findAllByEnabled(1)).thenReturn(enabled);
        Iterable<Customer> result = customerService.findAllByEnabledTrue();
        assertNotNull(result);
        verify(customerRepository).findAllByEnabled(1);
    }

    @Test
    void testFindAllByEnabledFalse() {
        List<Customer> disabled = Collections.singletonList(customer2);
        when(customerRepository.findAllByEnabled(0)).thenReturn(disabled);
        Iterable<Customer> result = customerService.findAllByEnabledFalse();
        assertNotNull(result);
        verify(customerRepository).findAllByEnabled(0);
    }

    @Test
    void testFindOneByEnabledTrueAndName() {
        when(customerRepository.findOneByEnabledAndName(1, "Acme")).thenReturn(customer1);
        Customer result = customerService.findOneByEnabledTrueAndName("Acme");
        assertNotNull(result);
        assertEquals("Acme", result.getName());
        verify(customerRepository).findOneByEnabledAndName(1, "Acme");
    }

    @Test
    void testFindOneByEnabledFalseAndName() {
        when(customerRepository.findOneByEnabledAndName(0, "Beta")).thenReturn(customer2);
        Customer result = customerService.findOneByEnabledFalseAndName("Beta");
        assertNotNull(result);
        assertEquals("Beta", result.getName());
        verify(customerRepository).findOneByEnabledAndName(0, "Beta");
    }

    @Test
    void testFindOneByName() {
        when(customerRepository.findOneByName("Acme")).thenReturn(customer1);
        Customer result = customerService.findOneByName("Acme");
        assertNotNull(result);
        verify(customerRepository).findOneByName("Acme");
    }

    @Test
    void testFindByEnabledTrueAndEmail() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndEmail(1, "acme@test.com")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndEmail("acme@test.com");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(1, "acme@test.com");
    }

    @Test
    void testFindByEnabledFalseAndEmail() {
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndEmail(0, "beta@test.com")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndEmail("beta@test.com");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(0, "beta@test.com");
    }

    @Test
    void testFindByEmail() {
        List<Customer> customers = Arrays.asList(customer1, customer2);
        when(customerRepository.findByEmail("test@test.com")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEmail("test@test.com");
        assertNotNull(result);
        verify(customerRepository).findByEmail("test@test.com");
    }

    @Test
    void testFindByEnabledTrueAndPhone() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndPhone(1, 123456789)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndPhone(123456789);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndPhone(1, 123456789);
    }

    @Test
    void testFindByEnabledFalseAndPhone() {
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndPhone(0, 987654321)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndPhone(987654321);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndPhone(0, 987654321);
    }

    @Test
    void testFindByPhone() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByPhone(123456789)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByPhone(123456789);
        assertNotNull(result);
        verify(customerRepository).findByPhone(123456789);
    }

    @Test
    void testFindByEnabledTrueAndCategories() {
        Set<Category> categories = new HashSet<>();
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("VIP");
        categories.add(cat);
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndCategories(1, categories)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndCategories(categories);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCategories(1, categories);
    }

    @Test
    void testFindByEnabledFalseAndCategories() {
        Set<Category> categories = new HashSet<>();
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndCategories(0, categories)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndCategories(categories);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCategories(0, categories);
    }

    @Test
    void testFindByCategories() {
        Set<Category> categories = new HashSet<>();
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByCategories(categories)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByCategories(categories);
        assertNotNull(result);
        verify(customerRepository).findByCategories(categories);
    }

    @Test
    void testFindByEnabledTrueAndFirstName() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndFirstName(1, "John")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstName("John");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstName(1, "John");
    }

    @Test
    void testFindByEnabledFalseAndFirstName() {
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndFirstName(0, "Jane")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndFirstName("Jane");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstName(0, "Jane");
    }

    @Test
    void testFindByFirstName() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByFirstName("John")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByFirstName("John");
        assertNotNull(result);
        verify(customerRepository).findByFirstName("John");
    }

    @Test
    void testFindByEnabledTrueAndLastName() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndLastName(1, "Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndLastName("Doe");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndLastName(1, "Doe");
    }

    @Test
    void testFindByEnabledFalseAndLastName() {
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndLastName(0, "Smith")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndLastName("Smith");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndLastName(0, "Smith");
    }

    @Test
    void testFindByLastName() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByLastName("Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByLastName("Doe");
        assertNotNull(result);
        verify(customerRepository).findByLastName("Doe");
    }

    @Test
    void testFindByEnabledTrueAndFirstNameAndLastName() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndFirstNameAndLastName(1, "John", "Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstNameAndLastName("John", "Doe");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstNameAndLastName(1, "John", "Doe");
    }

    @Test
    void testFindByEnabledFalseAndFirstNameAndLastName() {
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndFirstNameAndLastName(0, "Jane", "Smith")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndFirstNameAndLastName("Jane", "Smith");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstNameAndLastName(0, "Jane", "Smith");
    }

    @Test
    void testFindByFirstNameAndLastName() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByFirstNameAndLastName("John", "Doe");
        assertNotNull(result);
        verify(customerRepository).findByFirstNameAndLastName("John", "Doe");
    }

    @Test
    void testFindByEnabledTrueAndCity() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndCity(1, "NYC")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndCity("NYC");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCity(1, "NYC");
    }

    @Test
    void testFindByEnabledFalseAndCity() {
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndCity(0, "LA")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndCity("LA");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCity(0, "LA");
    }

    @Test
    void testFindByCity() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByCity("NYC")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByCity("NYC");
        assertNotNull(result);
        verify(customerRepository).findByCity("NYC");
    }

    @Test
    void testFindByEnabledTrueAndCityAndAddress() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByEnabledAndCityAndAddress(1, "NYC", "123 Main")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndCityAndAddress("NYC", "123 Main");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCityAndAddress(1, "NYC", "123 Main");
    }

    @Test
    void testFindByEnabledFalseAndCityAndAddress() {
        List<Customer> customers = Collections.singletonList(customer2);
        when(customerRepository.findByEnabledAndCityAndAddress(0, "LA", "456 Oak")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledFalseAndCityAndAddress("LA", "456 Oak");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCityAndAddress(0, "LA", "456 Oak");
    }

    @Test
    void testFindByCityAndAddress() {
        List<Customer> customers = Collections.singletonList(customer1);
        when(customerRepository.findByCityAndAddress("NYC", "123 Main")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByCityAndAddress("NYC", "123 Main");
        assertNotNull(result);
        verify(customerRepository).findByCityAndAddress("NYC", "123 Main");
    }

    @Test
    void testSaveCustomer() {
        Customer newCustomer = Customer.builder()
                .name("NewCo").email("new@test.com").build();
        customerService.saveCustomer(newCustomer);
        assertEquals(1, newCustomer.getEnabled());
        verify(customerRepository).save(newCustomer);
    }

    @Test
    void testSaveCustomer_SetsEnabledToOne() {
        Customer c = new Customer();
        c.setEnabled(0);
        customerService.saveCustomer(c);
        assertEquals(1, c.getEnabled());
    }
}
