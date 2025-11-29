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
        contract = new Contract();
    }

    @Test
    void testContractBuilder() {
        Contract built = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .content("Test Content")
                .value(BigDecimal.valueOf(1000.00))
                .beginDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(Status.PROPOSED)
                .build();

        assertNotNull(built);
        assertEquals(1L, built.getId());
        assertEquals("Test Contract", built.getName());
        assertEquals("Test Content", built.getContent());
        assertEquals(BigDecimal.valueOf(1000.00), built.getValue());
        assertEquals(Status.PROPOSED, built.getStatus());
    }

    @Test
    void testContractAllArgsConstructor() {
        Customer customer = new Customer();
        User user = new User();

        Contract contract = new Contract(
                1L,
                "Test Contract",
                "Test Content",
                BigDecimal.valueOf(1000.00),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                Status.PROPOSED,
                customer,
                user
        );

        assertNotNull(contract);
        assertEquals(1L, contract.getId());
        assertEquals("Test Contract", contract.getName());
    }

    @Test
    void testContractNoArgsConstructor() {
        Contract contract = new Contract();
        assertNotNull(contract);
    }

    @Test
    void testGettersAndSetters() {
        contract.setId(1L);
        contract.setName("Test Contract");
        contract.setContent("Test Content");
        contract.setValue(BigDecimal.valueOf(1000.00));
        contract.setBeginDate(LocalDate.of(2025, 1, 1));
        contract.setEndDate(LocalDate.of(2025, 12, 31));
        contract.setStatus(Status.PROPOSED);

        assertEquals(1L, contract.getId());
        assertEquals("Test Contract", contract.getName());
        assertEquals("Test Content", contract.getContent());
        assertEquals(BigDecimal.valueOf(1000.00), contract.getValue());
        assertEquals(LocalDate.of(2025, 1, 1), contract.getBeginDate());
        assertEquals(LocalDate.of(2025, 12, 31), contract.getEndDate());
        assertEquals(Status.PROPOSED, contract.getStatus());
    }

    @Test
    void testSetCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        contract.setCustomer(customer);

        assertNotNull(contract.getCustomer());
        assertEquals(1L, contract.getCustomer().getId());
    }

    @Test
    void testSetUser() {
        User user = new User();
        user.setId(1L);
        contract.setUser(user);

        assertNotNull(contract.getUser());
        assertEquals(1L, contract.getUser().getId());
    }

    @Test
    void testNullValues() {
        contract.setName(null);
        contract.setContent(null);
        contract.setValue(null);
        contract.setBeginDate(null);
        contract.setEndDate(null);
        contract.setStatus(null);
        contract.setCustomer(null);
        contract.setUser(null);

        assertNull(contract.getName());
        assertNull(contract.getContent());
        assertNull(contract.getValue());
        assertNull(contract.getBeginDate());
        assertNull(contract.getEndDate());
        assertNull(contract.getStatus());
        assertNull(contract.getCustomer());
        assertNull(contract.getUser());
    }

    @Test
    void testValueWithDecimalPlaces() {
        contract.setValue(BigDecimal.valueOf(1234.56));
        assertEquals(BigDecimal.valueOf(1234.56), contract.getValue());
    }

    @Test
    void testDateRangeValidation() {
        LocalDate beginDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        contract.setBeginDate(beginDate);
        contract.setEndDate(endDate);

        assertTrue(contract.getEndDate().isAfter(contract.getBeginDate()));
    }
}
