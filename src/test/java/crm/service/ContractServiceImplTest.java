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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractServiceImpl Tests")
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Contract contract;
    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).name("Test Customer").build();
        user = User.builder().id(1L).username("testuser").build();

        contract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .value(new BigDecimal("10000.00"))
                .beginDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status(Status.PROPOSED)
                .customer(customer)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Should find contract by name")
    void testFindByName() {
        when(contractRepository.findByName("Test Contract")).thenReturn(contract);

        Contract result = contractService.findByName("Test Contract");

        assertNotNull(result);
        assertEquals("Test Contract", result.getName());
        verify(contractRepository, times(1)).findByName("Test Contract");
    }

    @Test
    @DisplayName("Should list all contracts")
    void testListAllContracts() {
        when(contractRepository.findAll()).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.listAllContracts();

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should show contract by id")
    void testShowContract() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        Contract result = contractService.showContract(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(contractRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return null when contract not found")
    void testShowContractNotFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        Contract result = contractService.showContract(999L);

        assertNull(result);
        verify(contractRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find contracts by value less than or equal")
    void testFindAllByValueLessThanEqual() {
        when(contractRepository.findAllByValueLessThanEqual(any(BigDecimal.class)))
                .thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByValueLessThanEqual(new BigDecimal("20000.00"));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByValueLessThanEqual(any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should find contracts by value greater than or equal")
    void testFindAllByValueGreaterThanEqual() {
        when(contractRepository.findAllByValueGreaterThanEqual(any(BigDecimal.class)))
                .thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByValueGreaterThanEqual(new BigDecimal("5000.00"));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByValueGreaterThanEqual(any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should find contracts by begin date")
    void testFindAllByBeginDate() {
        when(contractRepository.findAllByBeginDate(any(LocalDate.class))).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByBeginDate(LocalDate.of(2024, 1, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByBeginDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find contracts by begin date before")
    void testFindAllByBeginDateBefore() {
        when(contractRepository.findAllByBeginDateBefore(any(LocalDate.class))).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByBeginDateBefore(LocalDate.of(2024, 6, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByBeginDateBefore(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find contracts by begin date after")
    void testFindAllByBeginDateAfter() {
        when(contractRepository.findAllByBeginDateAfter(any(LocalDate.class))).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByBeginDateAfter(LocalDate.of(2023, 12, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByBeginDateAfter(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find contracts by end date")
    void testFindAllByEndDate() {
        when(contractRepository.findAllByEndDate(any(LocalDate.class))).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByEndDate(LocalDate.of(2024, 12, 31));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByEndDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find contracts by end date before")
    void testFindAllByEndDateBefore() {
        when(contractRepository.findAllByEndDateBefore(any(LocalDate.class))).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByEndDateBefore(LocalDate.of(2025, 1, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByEndDateBefore(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find contracts by end date after")
    void testFindAllByEndDateAfter() {
        when(contractRepository.findAllByEndDateAfter(any(LocalDate.class))).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByEndDateAfter(LocalDate.of(2024, 6, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByEndDateAfter(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find contracts by status")
    void testFindAllByStatus() {
        when(contractRepository.findAllByStatus(Status.PROPOSED)).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByStatus(Status.PROPOSED);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByStatus(Status.PROPOSED);
    }

    @Test
    @DisplayName("Should find contracts by customer")
    void testFindAllByCustomer() {
        when(contractRepository.findAllByCustomer(customer)).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByCustomer(customer);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByCustomer(customer);
    }

    @Test
    @DisplayName("Should find contracts by customer and user")
    void testFindAllByCustomerAndUser() {
        when(contractRepository.findAllByCustomerAndUser(customer, user)).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByCustomerAndUser(customer, user);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByCustomerAndUser(customer, user);
    }

    @Test
    @DisplayName("Should find contracts by user")
    void testFindAllByUser() {
        when(contractRepository.findAllByUser(user)).thenReturn(Arrays.asList(contract));

        Iterable<Contract> result = contractService.findAllByUser(user);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByUser(user);
    }

    @Test
    @DisplayName("Should save contract")
    void testSaveContract() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        when(customerRepository.saveAll(any())).thenReturn(Arrays.asList(customer));
        when(userRepository.saveAll(any())).thenReturn(Arrays.asList(user));
        when(contractRepository.save(contract)).thenReturn(contract);

        contractService.saveContract(contract);

        verify(customerRepository, times(1)).findAll();
        verify(userRepository, times(1)).findAll();
        verify(customerRepository, times(1)).saveAll(any());
        verify(userRepository, times(1)).saveAll(any());
        verify(contractRepository, times(1)).save(contract);
    }

    @Test
    @DisplayName("Should handle null contract in save")
    void testSaveContractNull() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList());
        when(userRepository.findAll()).thenReturn(Arrays.asList());
        when(customerRepository.saveAll(any())).thenReturn(Arrays.asList());
        when(userRepository.saveAll(any())).thenReturn(Arrays.asList());

        contractService.saveContract(null);

        verify(customerRepository, times(1)).findAll();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return null when finding by null name")
    void testFindByNameNull() {
        when(contractRepository.findByName(null)).thenReturn(null);

        Contract result = contractService.findByName(null);

        assertNull(result);
        verify(contractRepository, times(1)).findByName(null);
    }
}
