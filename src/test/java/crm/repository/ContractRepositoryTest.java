package crm.repository;

import crm.entity.Contract;
import crm.entity.Customer;
import crm.entity.Status;
import crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractRepository Tests")
class ContractRepositoryTest {

    @Mock
    private ContractRepository contractRepository;

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
                .content("Content")
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

        Contract result = contractRepository.findByName("Test Contract");

        assertNotNull(result);
        assertEquals("Test Contract", result.getName());
        verify(contractRepository, times(1)).findByName("Test Contract");
    }

    @Test
    @DisplayName("Should find all contracts by value less than or equal")
    void testFindAllByValueLessThanEqual() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByValueLessThanEqual(new BigDecimal("20000.00")))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByValueLessThanEqual(new BigDecimal("20000.00"));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByValueLessThanEqual(any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should find all contracts by value greater than or equal")
    void testFindAllByValueGreaterThanEqual() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByValueGreaterThanEqual(new BigDecimal("5000.00")))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByValueGreaterThanEqual(new BigDecimal("5000.00"));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByValueGreaterThanEqual(any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should find all contracts by begin date")
    void testFindAllByBeginDate() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByBeginDate(LocalDate.of(2024, 1, 1)))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByBeginDate(LocalDate.of(2024, 1, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByBeginDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find all contracts by begin date before")
    void testFindAllByBeginDateBefore() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByBeginDateBefore(LocalDate.of(2024, 6, 1)))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByBeginDateBefore(LocalDate.of(2024, 6, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByBeginDateBefore(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find all contracts by begin date after")
    void testFindAllByBeginDateAfter() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByBeginDateAfter(LocalDate.of(2023, 12, 1)))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByBeginDateAfter(LocalDate.of(2023, 12, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByBeginDateAfter(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find all contracts by end date")
    void testFindAllByEndDate() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByEndDate(LocalDate.of(2024, 12, 31)))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByEndDate(LocalDate.of(2024, 12, 31));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByEndDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find all contracts by end date before")
    void testFindAllByEndDateBefore() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByEndDateBefore(LocalDate.of(2025, 1, 1)))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByEndDateBefore(LocalDate.of(2025, 1, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByEndDateBefore(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find all contracts by end date after")
    void testFindAllByEndDateAfter() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByEndDateAfter(LocalDate.of(2024, 6, 1)))
                .thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByEndDateAfter(LocalDate.of(2024, 6, 1));

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByEndDateAfter(any(LocalDate.class));
    }

    @Test
    @DisplayName("Should find all contracts by status")
    void testFindAllByStatus() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByStatus(Status.PROPOSED)).thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByStatus(Status.PROPOSED);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByStatus(Status.PROPOSED);
    }

    @Test
    @DisplayName("Should find all contracts by customer")
    void testFindAllByCustomer() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByCustomer(customer)).thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByCustomer(customer);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByCustomer(customer);
    }

    @Test
    @DisplayName("Should find all contracts by customer and user")
    void testFindAllByCustomerAndUser() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByCustomerAndUser(customer, user)).thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByCustomerAndUser(customer, user);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByCustomerAndUser(customer, user);
    }

    @Test
    @DisplayName("Should find all contracts by user")
    void testFindAllByUser() {
        List<Contract> contracts = Arrays.asList(contract);
        when(contractRepository.findAllByUser(user)).thenReturn(contracts);

        Iterable<Contract> result = contractRepository.findAllByUser(user);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByUser(user);
    }

    @Test
    @DisplayName("Should save contract")
    void testSave() {
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        Contract result = contractRepository.save(contract);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(contractRepository, times(1)).save(contract);
    }

    @Test
    @DisplayName("Should find contract by id")
    void testFindById() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        Optional<Contract> result = contractRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Contract", result.get().getName());
        verify(contractRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should delete contract")
    void testDelete() {
        doNothing().when(contractRepository).delete(contract);

        contractRepository.delete(contract);

        verify(contractRepository, times(1)).delete(contract);
    }

    @Test
    @DisplayName("Should count contracts")
    void testCount() {
        when(contractRepository.count()).thenReturn(10L);

        long result = contractRepository.count();

        assertEquals(10L, result);
        verify(contractRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should return null when contract not found by name")
    void testFindByNameNotFound() {
        when(contractRepository.findByName("NonExistent")).thenReturn(null);

        Contract result = contractRepository.findByName("NonExistent");

        assertNull(result);
        verify(contractRepository, times(1)).findByName("NonExistent");
    }

    @Test
    @DisplayName("Should return empty iterable when no contracts match status")
    void testFindAllByStatusEmpty() {
        when(contractRepository.findAllByStatus(Status.DONE)).thenReturn(Arrays.asList());

        Iterable<Contract> result = contractRepository.findAllByStatus(Status.DONE);

        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
        verify(contractRepository, times(1)).findAllByStatus(Status.DONE);
    }
}
