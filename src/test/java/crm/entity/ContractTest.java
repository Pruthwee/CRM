package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ContractTest {

    private Contract contract;

    @BeforeEach
    public void setUp() {
        contract = new Contract();
    }

    @Test
    public void testContractConstructor() {
        assertNotNull(contract);
    }

    @Test
    public void testContractBuilderPattern() {
        Customer customer = new Customer();
        User user = new User();
        LocalDate beginDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Contract builtContract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .content("Test Content")
                .value(new BigDecimal("10000.00"))
                .beginDate(beginDate)
                .endDate(endDate)
                .status(Status.PROPOSED)
                .customer(customer)
                .user(user)
                .build();

        assertNotNull(builtContract);
        assertEquals("Test Contract", builtContract.getName());
        assertEquals(new BigDecimal("10000.00"), builtContract.getValue());
    }

    @Test
    public void testAllArgsConstructor() {
        Customer customer = new Customer();
        User user = new User();
        LocalDate beginDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Contract fullContract = new Contract(1L, "Test", "Content", new BigDecimal("10000.00"), beginDate, endDate, Status.PROPOSED, customer, user);
        assertNotNull(fullContract);
        assertEquals("Test", fullContract.getName());
    }

    @Test
    public void testSetAndGetId() {
        contract.setId(1L);
        assertEquals(1L, contract.getId());
    }

    @Test
    public void testSetAndGetName() {
        contract.setName("Test Contract");
        assertEquals("Test Contract", contract.getName());
    }

    @Test
    public void testSetAndGetContent() {
        contract.setContent("Test Content");
        assertEquals("Test Content", contract.getContent());
    }

    @Test
    public void testSetAndGetValue() {
        BigDecimal value = new BigDecimal("10000.00");
        contract.setValue(value);
        assertEquals(value, contract.getValue());
    }

    @Test
    public void testSetAndGetBeginDate() {
        LocalDate beginDate = LocalDate.of(2024, 1, 1);
        contract.setBeginDate(beginDate);
        assertEquals(beginDate, contract.getBeginDate());
    }

    @Test
    public void testSetAndGetEndDate() {
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        contract.setEndDate(endDate);
        assertEquals(endDate, contract.getEndDate());
    }

    @Test
    public void testSetAndGetStatus() {
        contract.setStatus(Status.PROPOSED);
        assertEquals(Status.PROPOSED, contract.getStatus());
    }

    @Test
    public void testSetAndGetCustomer() {
        Customer customer = new Customer();
        contract.setCustomer(customer);
        assertEquals(customer, contract.getCustomer());
    }

    @Test
    public void testSetAndGetUser() {
        User user = new User();
        contract.setUser(user);
        assertEquals(user, contract.getUser());
    }

    @Test
    public void testContractWithNullValues() {
        contract.setName(null);
        contract.setValue(null);
        assertNull(contract.getName());
        assertNull(contract.getValue());
    }

    @Test
    public void testContractWithAllStatuses() {
        contract.setStatus(Status.PROPOSED);
        assertEquals(Status.PROPOSED, contract.getStatus());

        contract.setStatus(Status.NEGOTIATED);
        assertEquals(Status.NEGOTIATED, contract.getStatus());

        contract.setStatus(Status.IMPLEMENTED);
        assertEquals(Status.IMPLEMENTED, contract.getStatus());

        contract.setStatus(Status.DONE);
        assertEquals(Status.DONE, contract.getStatus());
    }
}
