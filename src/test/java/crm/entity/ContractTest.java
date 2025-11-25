package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ContractTest {

    private Contract contract;
    private Customer customer;
    private User user;

    @BeforeEach
    public void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("customer@test.com")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("user@test.com")
                .build();

        contract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .content("Test Content")
                .value(new BigDecimal("10000.00"))
                .beginDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(Status.PROPOSED)
                .customer(customer)
                .user(user)
                .build();
    }

    @Test
    public void testContractBuilder() {
        assertNotNull(contract);
        assertEquals(1L, contract.getId());
        assertEquals("Test Contract", contract.getName());
        assertEquals("Test Content", contract.getContent());
        assertEquals(new BigDecimal("10000.00"), contract.getValue());
        assertEquals(LocalDate.of(2025, 1, 1), contract.getBeginDate());
        assertEquals(LocalDate.of(2025, 12, 31), contract.getEndDate());
        assertEquals(Status.PROPOSED, contract.getStatus());
        assertEquals(customer, contract.getCustomer());
        assertEquals(user, contract.getUser());
    }

    @Test
    public void testContractNoArgsConstructor() {
        Contract emptyContract = new Contract();
        assertNotNull(emptyContract);
    }

    @Test
    public void testContractAllArgsConstructor() {
        Contract newContract = new Contract(
                2L, "Contract 2", "Content 2", new BigDecimal("20000.00"),
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 11, 30),
                Status.NEGOTIATED, customer, user
        );
        assertNotNull(newContract);
        assertEquals(2L, newContract.getId());
        assertEquals("Contract 2", newContract.getName());
        assertEquals(Status.NEGOTIATED, newContract.getStatus());
    }

    @Test
    public void testSettersAndGetters() {
        contract.setName("Updated Contract");
        assertEquals("Updated Contract", contract.getName());

        contract.setContent("Updated Content");
        assertEquals("Updated Content", contract.getContent());

        contract.setValue(new BigDecimal("15000.00"));
        assertEquals(new BigDecimal("15000.00"), contract.getValue());

        contract.setStatus(Status.DONE);
        assertEquals(Status.DONE, contract.getStatus());
    }

    @Test
    public void testContractWithNullValues() {
        Contract nullContract = new Contract();
        nullContract.setId(null);
        nullContract.setName(null);
        nullContract.setContent(null);
        nullContract.setValue(null);
        nullContract.setBeginDate(null);
        nullContract.setEndDate(null);
        nullContract.setStatus(null);
        nullContract.setCustomer(null);
        nullContract.setUser(null);

        assertNull(nullContract.getId());
        assertNull(nullContract.getName());
        assertNull(nullContract.getContent());
        assertNull(nullContract.getValue());
        assertNull(nullContract.getBeginDate());
        assertNull(nullContract.getEndDate());
        assertNull(nullContract.getStatus());
        assertNull(nullContract.getCustomer());
        assertNull(nullContract.getUser());
    }

    @Test
    public void testContractDateRange() {
        LocalDate beginDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        contract.setBeginDate(beginDate);
        contract.setEndDate(endDate);

        assertTrue(contract.getBeginDate().isBefore(contract.getEndDate()));
    }

    @Test
    public void testContractValuePositive() {
        contract.setValue(new BigDecimal("50000.00"));
        assertTrue(contract.getValue().compareTo(BigDecimal.ZERO) > 0);
    }
}
