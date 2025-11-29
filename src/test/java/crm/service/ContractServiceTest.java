package crm.service;

import crm.entity.Contract;
import crm.entity.Customer;
import crm.entity.Status;
import crm.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Test
    void testContractServiceInterface() {
        assertNotNull(ContractService.class);
        assertTrue(ContractService.class.isInterface());
    }

    @Test
    void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findByName", String.class));
    }

    @Test
    void testListAllContractsMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("listAllContracts"));
    }

    @Test
    void testShowContractMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("showContract", Long.class));
    }

    @Test
    void testFindAllByValueLessThanEqualMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByValueLessThanEqual", BigDecimal.class));
    }

    @Test
    void testFindAllByValueGreaterThanEqualMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByValueGreaterThanEqual", BigDecimal.class));
    }

    @Test
    void testFindAllByBeginDateMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByBeginDate", LocalDate.class));
    }

    @Test
    void testFindAllByBeginDateBeforeMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByBeginDateBefore", LocalDate.class));
    }

    @Test
    void testFindAllByBeginDateAfterMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByBeginDateAfter", LocalDate.class));
    }

    @Test
    void testFindAllByEndDateMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByEndDate", LocalDate.class));
    }

    @Test
    void testFindAllByEndDateBeforeMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByEndDateBefore", LocalDate.class));
    }

    @Test
    void testFindAllByEndDateAfterMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByEndDateAfter", LocalDate.class));
    }

    @Test
    void testFindAllByStatusMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByStatus", Status.class));
    }

    @Test
    void testFindAllByCustomerMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByCustomer", Customer.class));
    }

    @Test
    void testFindAllByCustomerAndUserMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByCustomerAndUser", Customer.class, User.class));
    }

    @Test
    void testFindAllByUserMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByUser", User.class));
    }

    @Test
    void testSaveContractMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("saveContract", Contract.class));
    }
}
