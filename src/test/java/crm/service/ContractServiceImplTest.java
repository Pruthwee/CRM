package crm.service;

import crm.entity.Contract;
import crm.entity.Customer;
import crm.entity.Status;
import crm.entity.User;
import crm.repository.ContractRepository;
import crm.repository.CustomerRepository;
import crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Contract testContract;
    private Customer testCustomer;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("user@example.com")
                .build();

        testContract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .value(new BigDecimal("10000.00"))
                .beginDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .status(Status.PROPOSED)
                .customer(testCustomer)
                .user(testUser)
                .build();
    }

    @Test
    void testConstructor() {
        assertNotNull(contractService);
    }

    @Test
    void testFindByName() {
        when(contractRepository.findByName("Test Contract")).thenReturn(testContract);

        Contract result = contractService.findByName("Test Contract");

        assertNotNull(result);
        assertEquals("Test Contract", result.getName());
        verify(contractRepository).findByName("Test Contract");
    }

    @Test
    void testFindByNameNotFound() {
        when(contractRepository.findByName("NonExistent")).thenReturn(null);

        Contract result = contractService.findByName("NonExistent");

        assertNull(result);
        verify(contractRepository).findByName("NonExistent");
    }

    @Test
    void testFindByNameNull() {
        when(contractRepository.findByName(null)).thenReturn(null);

        Contract result = contractService.findByName(null);

        assertNull(result);
        verify(contractRepository).findByName(null);
    }

    @Test
    void testListAllContracts() {
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAll()).thenReturn(contracts);

        Iterable<Contract> result = contractService.listAllContracts();

        assertNotNull(result);
        verify(contractRepository).findAll();
    }

    @Test
    void testListAllContractsEmpty() {
        when(contractRepository.findAll()).thenReturn(Arrays.asList());

        Iterable<Contract> result = contractService.listAllContracts();

        assertNotNull(result);
        verify(contractRepository).findAll();
    }

    @Test
    void testShowContract() {
        when(contractRepository.findById(1L)).thenReturn(java.util.Optional.of(testContract));

        Contract result = contractService.showContract(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(contractRepository).findById(1L);
    }

    @Test
    void testShowContractNotFound() {
        when(contractRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        Contract result = contractService.showContract(999L);

        assertNull(result);
        verify(contractRepository).findById(999L);
    }

    @Test
    void testShowContractNullId() {
        when(contractRepository.findById(null)).thenReturn(java.util.Optional.empty());

        Contract result = contractService.showContract(null);

        assertNull(result);
        verify(contractRepository).findById(null);
    }

    @Test
    void testFindAllByValueLessThanEqual() {
        BigDecimal value = new BigDecimal("10000.00");
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByValueLessThanEqual(value)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByValueLessThanEqual(value);

        assertNotNull(result);
        verify(contractRepository).findAllByValueLessThanEqual(value);
    }

    @Test
    void testFindAllByValueLessThanEqualNull() {
        when(contractRepository.findAllByValueLessThanEqual(null)).thenReturn(Arrays.asList());

        Iterable<Contract> result = contractService.findAllByValueLessThanEqual(null);

        assertNotNull(result);
        verify(contractRepository).findAllByValueLessThanEqual(null);
    }

    @Test
    void testFindAllByValueGreaterThanEqual() {
        BigDecimal value = new BigDecimal("5000.00");
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByValueGreaterThanEqual(value)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByValueGreaterThanEqual(value);

        assertNotNull(result);
        verify(contractRepository).findAllByValueGreaterThanEqual(value);
    }

    @Test
    void testFindAllByBeginDate() {
        LocalDate date = LocalDate.now();
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByBeginDate(date)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByBeginDate(date);

        assertNotNull(result);
        verify(contractRepository).findAllByBeginDate(date);
    }

    @Test
    void testFindAllByBeginDateBefore() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByBeginDateBefore(date)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByBeginDateBefore(date);

        assertNotNull(result);
        verify(contractRepository).findAllByBeginDateBefore(date);
    }

    @Test
    void testFindAllByBeginDateAfter() {
        LocalDate date = LocalDate.now().minusDays(1);
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByBeginDateAfter(date)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByBeginDateAfter(date);

        assertNotNull(result);
        verify(contractRepository).findAllByBeginDateAfter(date);
    }

    @Test
    void testFindAllByEndDate() {
        LocalDate date = LocalDate.now().plusMonths(6);
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByEndDate(date)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByEndDate(date);

        assertNotNull(result);
        verify(contractRepository).findAllByEndDate(date);
    }

    @Test
    void testFindAllByEndDateBefore() {
        LocalDate date = LocalDate.now().plusMonths(7);
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByEndDateBefore(date)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByEndDateBefore(date);

        assertNotNull(result);
        verify(contractRepository).findAllByEndDateBefore(date);
    }

    @Test
    void testFindAllByEndDateAfter() {
        LocalDate date = LocalDate.now().plusMonths(5);
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByEndDateAfter(date)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByEndDateAfter(date);

        assertNotNull(result);
        verify(contractRepository).findAllByEndDateAfter(date);
    }

    @Test
    void testFindAllByStatus() {
        Status status = Status.PROPOSED;
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByStatus(status)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByStatus(status);

        assertNotNull(result);
        verify(contractRepository).findAllByStatus(status);
    }

    @Test
    void testFindAllByCustomer() {
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByCustomer(testCustomer)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByCustomer(testCustomer);

        assertNotNull(result);
        verify(contractRepository).findAllByCustomer(testCustomer);
    }

    @Test
    void testFindAllByCustomerAndUser() {
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByCustomerAndUser(testCustomer, testUser)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByCustomerAndUser(testCustomer, testUser);

        assertNotNull(result);
        verify(contractRepository).findAllByCustomerAndUser(testCustomer, testUser);
    }

    @Test
    void testFindAllByUser() {
        List<Contract> contracts = Arrays.asList(testContract);
        when(contractRepository.findAllByUser(testUser)).thenReturn(contracts);

        Iterable<Contract> result = contractService.findAllByUser(testUser);

        assertNotNull(result);
        verify(contractRepository).findAllByUser(testUser);
    }

    @Test
    void testSaveContract() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(testCustomer));
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(contractRepository.save(testContract)).thenReturn(testContract);

        contractService.saveContract(testContract);

        verify(customerRepository).saveAll(anyIterable());
        verify(userRepository).saveAll(anyIterable());
        verify(contractRepository).save(testContract);
    }

    @Test
    void testSaveContractNull() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList());
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        assertDoesNotThrow(() -> contractService.saveContract(null));
    }
}
