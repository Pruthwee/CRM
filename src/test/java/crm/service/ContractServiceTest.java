package crm.service;

import crm.entity.Contract;
import crm.entity.Customer;
import crm.entity.Status;
import crm.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ContractServiceTest {

    @Test
    public void testContractServiceInterfaceExists() {
        assertNotNull(ContractService.class);
    }

    @Test
    public void testFindByNameMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findByName", String.class));
    }

    @Test
    public void testListAllContractsMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("listAllContracts"));
    }

    @Test
    public void testShowContractMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("showContract", Long.class));
    }

    @Test
    public void testFindAllByValueLessThanEqualMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByValueLessThanEqual", BigDecimal.class));
    }

    @Test
    public void testFindAllByValueGreaterThanEqualMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByValueGreaterThanEqual", BigDecimal.class));
    }

    @Test
    public void testFindAllByBeginDateMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByBeginDate", LocalDate.class));
    }

    @Test
    public void testFindAllByBeginDateBeforeMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByBeginDateBefore", LocalDate.class));
    }

    @Test
    public void testFindAllByBeginDateAfterMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByBeginDateAfter", LocalDate.class));
    }

    @Test
    public void testFindAllByEndDateMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByEndDate", LocalDate.class));
    }

    @Test
    public void testFindAllByEndDateBeforeMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByEndDateBefore", LocalDate.class));
    }

    @Test
    public void testFindAllByEndDateAfterMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByEndDateAfter", LocalDate.class));
    }

    @Test
    public void testFindAllByStatusMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByStatus", Status.class));
    }

    @Test
    public void testFindAllByCustomerMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByCustomer", Customer.class));
    }

    @Test
    public void testFindAllByCustomerAndUserMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByCustomerAndUser", Customer.class, User.class));
    }

    @Test
    public void testFindAllByUserMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("findAllByUser", User.class));
    }

    @Test
    public void testSaveContractMethodExists() throws Exception {
        assertNotNull(ContractService.class.getDeclaredMethod("saveContract", Contract.class));
    }
}
