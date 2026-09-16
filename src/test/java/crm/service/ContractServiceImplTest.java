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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    private Contract contract;
    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("TestCustomer");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        contract = new Contract();
        contract.setId(1L);
        contract.setName("Contract-001");
        contract.setValue(new BigDecimal("1000.00"));
        contract.setBeginDate(LocalDate.of(2024, 1, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));
        contract.setStatus(Status.PROPOSED);
        contract.setCustomer(customer);
        contract.setUser(user);
    }

    @Test
    void testConstructor() {
        ContractServiceImpl service = new ContractServiceImpl(contractRepository, customerRepository, userRepository);
        assertNotNull(service);
    }

    @Test
    void testFindByName_found() {
        when(contractRepository.findByName("Contract-001")).thenReturn(contract);
        Contract result = contractService.findByName("Contract-001");
        assertNotNull(result);
        assertEquals("Contract-001", result.getName());
        verify(contractRepository).findByName("Contract-001");
    }

    @Test
    void testFindByName_notFound() {
        when(contractRepository.findByName("Unknown")).thenReturn(null);
        Contract result = contractService.findByName("Unknown");
        assertNull(result);
        verify(contractRepository).findByName("Unknown");
    }

    @Test
    void testListAllContracts() {
        when(contractRepository.findAll()).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.listAllContracts();
        assertNotNull(result);
        verify(contractRepository).findAll();
    }

    @Test
    void testShowContract_found() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        Contract result = contractService.showContract(1L);
        assertNotNull(result);
        assertEquals("Contract-001", result.getName());
        verify(contractRepository).findById(1L);
    }

    @Test
    void testShowContract_notFound() {
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());
        Contract result = contractService.showContract(99L);
        assertNull(result);
        verify(contractRepository).findById(99L);
    }

    @Test
    void testFindAllByValueLessThanEqual() {
        BigDecimal value = new BigDecimal("2000.00");
        when(contractRepository.findAllByValueLessThanEqual(value)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByValueLessThanEqual(value);
        assertNotNull(result);
        verify(contractRepository).findAllByValueLessThanEqual(value);
    }

    @Test
    void testFindAllByValueGreaterThanEqual() {
        BigDecimal value = new BigDecimal("500.00");
        when(contractRepository.findAllByValueGreaterThanEqual(value)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByValueGreaterThanEqual(value);
        assertNotNull(result);
        verify(contractRepository).findAllByValueGreaterThanEqual(value);
    }

    @Test
    void testFindAllByBeginDate() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        when(contractRepository.findAllByBeginDate(date)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByBeginDate(date);
        assertNotNull(result);
        verify(contractRepository).findAllByBeginDate(date);
    }

    @Test
    void testFindAllByBeginDateBefore() {
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(contractRepository.findAllByBeginDateBefore(date)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByBeginDateBefore(date);
        assertNotNull(result);
        verify(contractRepository).findAllByBeginDateBefore(date);
    }

    @Test
    void testFindAllByBeginDateAfter() {
        LocalDate date = LocalDate.of(2023, 12, 31);
        when(contractRepository.findAllByBeginDateAfter(date)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByBeginDateAfter(date);
        assertNotNull(result);
        verify(contractRepository).findAllByBeginDateAfter(date);
    }

    @Test
    void testFindAllByEndDate() {
        LocalDate date = LocalDate.of(2024, 12, 31);
        when(contractRepository.findAllByEndDate(date)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByEndDate(date);
        assertNotNull(result);
        verify(contractRepository).findAllByEndDate(date);
    }

    @Test
    void testFindAllByEndDateBefore() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        when(contractRepository.findAllByEndDateBefore(date)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByEndDateBefore(date);
        assertNotNull(result);
        verify(contractRepository).findAllByEndDateBefore(date);
    }

    @Test
    void testFindAllByEndDateAfter() {
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(contractRepository.findAllByEndDateAfter(date)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByEndDateAfter(date);
        assertNotNull(result);
        verify(contractRepository).findAllByEndDateAfter(date);
    }

    @Test
    void testFindAllByStatus_Proposed() {
        when(contractRepository.findAllByStatus(Status.PROPOSED)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByStatus(Status.PROPOSED);
        assertNotNull(result);
        verify(contractRepository).findAllByStatus(Status.PROPOSED);
    }

    @Test
    void testFindAllByStatus_Done() {
        when(contractRepository.findAllByStatus(Status.DONE)).thenReturn(Collections.emptyList());
        Iterable<Contract> result = contractService.findAllByStatus(Status.DONE);
        assertNotNull(result);
        verify(contractRepository).findAllByStatus(Status.DONE);
    }

    @Test
    void testFindAllByCustomer() {
        when(contractRepository.findAllByCustomer(customer)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByCustomer(customer);
        assertNotNull(result);
        verify(contractRepository).findAllByCustomer(customer);
    }

    @Test
    void testFindAllByCustomerAndUser() {
        when(contractRepository.findAllByCustomerAndUser(customer, user)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByCustomerAndUser(customer, user);
        assertNotNull(result);
        verify(contractRepository).findAllByCustomerAndUser(customer, user);
    }

    @Test
    void testFindAllByUser() {
        when(contractRepository.findAllByUser(user)).thenReturn(Arrays.asList(contract));
        Iterable<Contract> result = contractService.findAllByUser(user);
        assertNotNull(result);
        verify(contractRepository).findAllByUser(user);
    }

    @Test
    void testSaveContract() {
        contractService.saveContract(contract);
        verify(contractRepository, times(1)).save(contract);
    }

    @Test
    void testSaveContract_newContract() {
        Contract newContract = new Contract();
        newContract.setName("New-Contract");
        contractService.saveContract(newContract);
        verify(contractRepository).save(newContract);
    }
}
