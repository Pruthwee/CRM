package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContractServiceTest {

    @Test
    public void testContractServiceInterface() {
        assertNotNull(ContractService.class);
        assertTrue(ContractService.class.isInterface());
    }

    @Test
    public void testFindByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findByName", String.class));
    }

    @Test
    public void testListAllContractsMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("listAllContracts"));
    }

    @Test
    public void testShowContractMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("showContract", Long.class));
    }

    @Test
    public void testSaveContractMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("saveContract", crm.entity.Contract.class));
    }

    @Test
    public void testFindAllByValueLessThanEqualMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByValueLessThanEqual", java.math.BigDecimal.class));
    }

    @Test
    public void testFindAllByValueGreaterThanEqualMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByValueGreaterThanEqual", java.math.BigDecimal.class));
    }

    @Test
    public void testFindAllByBeginDateMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByBeginDate", java.time.LocalDate.class));
    }

    @Test
    public void testFindAllByStatusMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByStatus", crm.entity.Status.class));
    }

    @Test
    public void testFindAllByCustomerMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByCustomer", crm.entity.Customer.class));
    }

    @Test
    public void testFindAllByUserMethodExists() throws NoSuchMethodException {
        assertNotNull(ContractService.class.getMethod("findAllByUser", crm.entity.User.class));
    }
}
