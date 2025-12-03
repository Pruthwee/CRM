package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContractTest {

    private Contract contract;

    @BeforeEach
    void setUp() {
        contract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .content("Contract content")
                .value(new BigDecimal("10000.00"))
                .beginDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .status(Status.PROPOSED)
                .build();
    }

    @Test
    void testConstructor() {
        assertNotNull(contract);
    }

    @Test
    void testBuilder() {
        Contract built = Contract.builder()
                .name("Builder Test")
                .value(new BigDecimal("5000"))
                .build();

        assertNotNull(built);
        assertEquals("Builder Test", built.getName());
    }

    @Test
    void testGettersAndSetters() {
        contract.setName("Updated Contract");
        assertEquals("Updated Contract", contract.getName());

        contract.setContent("Updated content");
        assertEquals("Updated content", contract.getContent());

        contract.setValue(new BigDecimal("20000"));
        assertEquals(new BigDecimal("20000"), contract.getValue());
    }

    @Test
    void testId() {
        assertEquals(1L, contract.getId());
        contract.setId(2L);
        assertEquals(2L, contract.getId());
    }

    @Test
    void testName() {
        assertEquals("Test Contract", contract.getName());
    }

    @Test
    void testNullName() {
        contract.setName(null);
        assertNull(contract.getName());
    }

    @Test
    void testValue() {
        assertEquals(new BigDecimal("10000.00"), contract.getValue());
    }

    @Test
    void testNullValue() {
        contract.setValue(null);
        assertNull(contract.getValue());
    }

    @Test
    void testBeginDate() {
        assertNotNull(contract.getBeginDate());
    }

    @Test
    void testEndDate() {
        assertNotNull(contract.getEndDate());
    }

    @Test
    void testStatus() {
        assertEquals(Status.PROPOSED, contract.getStatus());
        contract.setStatus(Status.DONE);
        assertEquals(Status.DONE, contract.getStatus());
    }

    @Test
    void testCustomer() {
        Customer customer = Customer.builder().id(1L).name("Test Customer").build();
        contract.setCustomer(customer);
        assertEquals(customer, contract.getCustomer());
    }

    @Test
    void testUser() {
        User user = User.builder().id(1L).username("testuser").build();
        contract.setUser(user);
        assertEquals(user, contract.getUser());
    }

    @Test
    void testNoArgsConstructor() {
        Contract empty = new Contract();
        assertNotNull(empty);
    }

    @Test
    void testAllArgsConstructor() {
        Contract full = new Contract(1L, "Name", "Content", new BigDecimal("1000"),
                LocalDate.now(), LocalDate.now(), Status.PROPOSED, null, null);
        assertNotNull(full);
        assertEquals("Name", full.getName());
    }
}
