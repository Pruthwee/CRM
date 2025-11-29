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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("customer@example.com")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("user@example.com")
                .build();

        testContract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .content("Test Content")
                .value(BigDecimal.valueOf(1000.00))
                .beginDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(Status.PROPOSED)
                .customer(testCustomer)
                .user(testUser)
                .build();
    }

    @Test
    void testConstructor() {
        ContractServiceImpl service = new ContractServiceImpl(contractRepository, customerRepository, userRepository);
        assertNotNull(service);
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
        when(contractRepository.findByName("Nonexistent")).thenReturn(null);

        Contract result = contractService.findByName("Nonexistent");

        assertNull(result);
        verify(contractRepository).findByName("Nonexistent");
    }

    @Test
    void testListAllContracts() {
        ArrayList<Contract> contracts = new ArrayList<>();
        contracts.add(testContract);
        when(contractRepository.findAll()).thenReturn(contracts);

        Iterable<Contract> result = contractService.listAllContracts();

        assertNotNull(result);
        verify(contractRepository).findAll();
    }

    @Test
    void testShowContract() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        Contract result = contractService.showContract(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(contractRepository).findById(1L);
    }

    @Test
    void testShowContractNotFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        Contract result = contractService.showContract(999L);

        assertNull(result);
        verify(contractRepository).findById(999L);
    }

    @Test
    void testFindAllByValueLessThanEqual() {
        BigDecimal value = BigDecimal.valueOf(1000.00);
        when(contractRepository.findAllByValueLessThanEqual(value)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByValueLessThanEqual(value);

        assertNotNull(result);
        verify(contractRepository).findAllByValueLessThanEqual(value);
    }

    @Test
    void testFindAllByValueGreaterThanEqual() {
        BigDecimal value = BigDecimal.valueOf(500.00);
        when(contractRepository.findAllByValueGreaterThanEqual(value)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByValueGreaterThanEqual(value);

        assertNotNull(result);
        verify(contractRepository).findAllByValueGreaterThanEqual(value);
    }

    @Test
    void testFindAllByBeginDate() {
        LocalDate date = LocalDate.now();
        when(contractRepository.findAllByBeginDate(date)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByBeginDate(date);

        assertNotNull(result);
        verify(contractRepository).findAllByBeginDate(date);
    }

    @Test
    void testFindAllByBeginDateBefore() {
        LocalDate date = LocalDate.now();
        when(contractRepository.findAllByBeginDateBefore(date)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByBeginDateBefore(date);

        assertNotNull(result);
        verify(contractRepository).findAllByBeginDateBefore(date);
    }

    @Test
    void testFindAllByBeginDateAfter() {
        LocalDate date = LocalDate.now();
        when(contractRepository.findAllByBeginDateAfter(date)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByBeginDateAfter(date);

        assertNotNull(result);
        verify(contractRepository).findAllByBeginDateAfter(date);
    }

    @Test
    void testFindAllByEndDate() {
        LocalDate date = LocalDate.now().plusDays(30);
        when(contractRepository.findAllByEndDate(date)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByEndDate(date);

        assertNotNull(result);
        verify(contractRepository).findAllByEndDate(date);
    }

    @Test
    void testFindAllByEndDateBefore() {
        LocalDate date = LocalDate.now();
        when(contractRepository.findAllByEndDateBefore(date)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByEndDateBefore(date);

        assertNotNull(result);
        verify(contractRepository).findAllByEndDateBefore(date);
    }

    @Test
    void testFindAllByEndDateAfter() {
        LocalDate date = LocalDate.now();
        when(contractRepository.findAllByEndDateAfter(date)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByEndDateAfter(date);

        assertNotNull(result);
        verify(contractRepository).findAllByEndDateAfter(date);
    }

    @Test
    void testFindAllByStatus() {
        when(contractRepository.findAllByStatus(Status.PROPOSED)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByStatus(Status.PROPOSED);

        assertNotNull(result);
        verify(contractRepository).findAllByStatus(Status.PROPOSED);
    }

    @Test
    void testFindAllByCustomer() {
        when(contractRepository.findAllByCustomer(testCustomer)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByCustomer(testCustomer);

        assertNotNull(result);
        verify(contractRepository).findAllByCustomer(testCustomer);
    }

    @Test
    void testFindAllByCustomerAndUser() {
        when(contractRepository.findAllByCustomerAndUser(testCustomer, testUser)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByCustomerAndUser(testCustomer, testUser);

        assertNotNull(result);
        verify(contractRepository).findAllByCustomerAndUser(testCustomer, testUser);
    }

    @Test
    void testFindAllByUser() {
        when(contractRepository.findAllByUser(testUser)).thenReturn(new ArrayList<>());

        Iterable<Contract> result = contractService.findAllByUser(testUser);

        assertNotNull(result);
        verify(contractRepository).findAllByUser(testUser);
    }

    @Test
    void testSaveContract() {
        when(customerRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(customerRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(userRepository.saveAll(any())).thenReturn(new ArrayList<>());

        contractService.saveContract(testContract);

        verify(contractRepository).save(testContract);
        verify(customerRepository).saveAll(any());
        verify(userRepository).saveAll(any());
    }
}
