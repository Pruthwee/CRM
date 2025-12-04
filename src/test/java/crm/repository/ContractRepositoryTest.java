package crm.repository;

import crm.entity.Customer;
import crm.entity.Status;
import crm.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ContractRepositoryTest {

    @Test
    public void testContractRepositoryInterfaceExists() {
        assertNotNull(ContractRepository.class);
    }

    @Test
    public void testFindByNameMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findByName", String.class));
    }

    @Test
    public void testFindAllByValueLessThanEqualMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByValueLessThanEqual", BigDecimal.class));
    }

    @Test
    public void testFindAllByValueGreaterThanEqualMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByValueGreaterThanEqual", BigDecimal.class));
    }

    @Test
    public void testFindAllByBeginDateMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByBeginDate", LocalDate.class));
    }

    @Test
    public void testFindAllByBeginDateBeforeMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByBeginDateBefore", LocalDate.class));
    }

    @Test
    public void testFindAllByBeginDateAfterMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByBeginDateAfter", LocalDate.class));
    }

    @Test
    public void testFindAllByEndDateMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByEndDate", LocalDate.class));
    }

    @Test
    public void testFindAllByEndDateBeforeMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByEndDateBefore", LocalDate.class));
    }

    @Test
    public void testFindAllByEndDateAfterMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByEndDateAfter", LocalDate.class));
    }

    @Test
    public void testFindAllByStatusMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByStatus", Status.class));
    }

    @Test
    public void testFindAllByCustomerMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByCustomer", Customer.class));
    }

    @Test
    public void testFindAllByCustomerAndUserMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByCustomerAndUser", Customer.class, User.class));
    }

    @Test
    public void testFindAllByUserMethodExists() throws Exception {
        assertNotNull(ContractRepository.class.getDeclaredMethod("findAllByUser", User.class));
    }
}
