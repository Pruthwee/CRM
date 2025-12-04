package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Contract Entity Tests")
class ContractTest {

    private Contract contract;
    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        contract = new Contract();
        customer = Customer.builder().id(1L).name("Test Customer").build();
        user = User.builder().id(1L).username("testuser").build();
    }

    @Test
    @DisplayName("Should create contract with default constructor")
    void testDefaultConstructor() {
        assertNotNull(contract);
        assertNull(contract.getId());
        assertNull(contract.getName());
    }

    @Test
    @DisplayName("Should create contract with builder")
    void testBuilderConstructor() {
        LocalDate beginDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Contract builtContract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .content("Contract content")
                .value(new BigDecimal("10000.00"))
                .beginDate(beginDate)
                .endDate(endDate)
                .status(Status.PROPOSED)
                .customer(customer)
                .user(user)
                .build();

        assertNotNull(builtContract);
        assertEquals(1L, builtContract.getId());
        assertEquals("Test Contract", builtContract.getName());
        assertEquals("Contract content", builtContract.getContent());
        assertEquals(new BigDecimal("10000.00"), builtContract.getValue());
        assertEquals(beginDate, builtContract.getBeginDate());
        assertEquals(endDate, builtContract.getEndDate());
        assertEquals(Status.PROPOSED, builtContract.getStatus());
        assertEquals(customer, builtContract.getCustomer());
        assertEquals(user, builtContract.getUser());
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testSettersAndGetters() {
        LocalDate beginDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        BigDecimal value = new BigDecimal("50000.50");

        contract.setId(2L);
        contract.setName("Service Contract");
        contract.setContent("Service details");
        contract.setValue(value);
        contract.setBeginDate(beginDate);
        contract.setEndDate(endDate);
        contract.setStatus(Status.NEGOTIATED);
        contract.setCustomer(customer);
        contract.setUser(user);

        assertEquals(2L, contract.getId());
        assertEquals("Service Contract", contract.getName());
        assertEquals("Service details", contract.getContent());
        assertEquals(value, contract.getValue());
        assertEquals(beginDate, contract.getBeginDate());
        assertEquals(endDate, contract.getEndDate());
        assertEquals(Status.NEGOTIATED, contract.getStatus());
        assertEquals(customer, contract.getCustomer());
        assertEquals(user, contract.getUser());
    }

    @Test
    @DisplayName("Should handle all status values")
    void testAllStatusValues() {
        contract.setStatus(Status.PROPOSED);
        assertEquals(Status.PROPOSED, contract.getStatus());

        contract.setStatus(Status.NEGOTIATED);
        assertEquals(Status.NEGOTIATED, contract.getStatus());

        contract.setStatus(Status.IMPLEMENTED);
        assertEquals(Status.IMPLEMENTED, contract.getStatus());

        contract.setStatus(Status.DONE);
        assertEquals(Status.DONE, contract.getStatus());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        contract.setId(null);
        contract.setName(null);
        contract.setContent(null);
        contract.setValue(null);
        contract.setBeginDate(null);
        contract.setEndDate(null);
        contract.setStatus(null);
        contract.setCustomer(null);
        contract.setUser(null);

        assertNull(contract.getId());
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
    @DisplayName("Should handle zero value")
    void testZeroValue() {
        BigDecimal zeroValue = BigDecimal.ZERO;
        contract.setValue(zeroValue);
        assertEquals(zeroValue, contract.getValue());
    }

    @Test
    @DisplayName("Should handle negative value")
    void testNegativeValue() {
        BigDecimal negativeValue = new BigDecimal("-1000.00");
        contract.setValue(negativeValue);
        assertEquals(negativeValue, contract.getValue());
    }

    @Test
    @DisplayName("Should handle very large value")
    void testVeryLargeValue() {
        BigDecimal largeValue = new BigDecimal("999999999999.99");
        contract.setValue(largeValue);
        assertEquals(largeValue, contract.getValue());
    }

    @Test
    @DisplayName("Should handle same begin and end dates")
    void testSameBeginAndEndDates() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        contract.setBeginDate(date);
        contract.setEndDate(date);
        assertEquals(date, contract.getBeginDate());
        assertEquals(date, contract.getEndDate());
    }

    @Test
    @DisplayName("Should handle end date before begin date")
    void testEndDateBeforeBeginDate() {
        LocalDate beginDate = LocalDate.of(2024, 12, 31);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        contract.setBeginDate(beginDate);
        contract.setEndDate(endDate);
        assertEquals(beginDate, contract.getBeginDate());
        assertEquals(endDate, contract.getEndDate());
        assertTrue(contract.getEndDate().isBefore(contract.getBeginDate()));
    }

    @Test
    @DisplayName("Should handle empty content")
    void testEmptyContent() {
        contract.setContent("");
        assertEquals("", contract.getContent());
    }

    @Test
    @DisplayName("Should handle long content")
    void testLongContent() {
        String longContent = "A".repeat(10000);
        contract.setContent(longContent);
        assertEquals(longContent, contract.getContent());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        Contract contract1 = Contract.builder()
                .id(1L)
                .name("Contract A")
                .build();

        Contract contract2 = Contract.builder()
                .id(1L)
                .name("Contract A")
                .build();

        assertEquals(contract1, contract2);
        assertEquals(contract1.hashCode(), contract2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        contract.setId(1L);
        contract.setName("Test Contract");
        String result = contract.toString();
        assertNotNull(result);
        assertTrue(result.contains("Contract"));
    }

    @Test
    @DisplayName("Should handle all args constructor")
    void testAllArgsConstructor() {
        LocalDate beginDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        BigDecimal value = new BigDecimal("25000.00");

        Contract newContract = new Contract(
                3L,
                "Full Contract",
                "Full content",
                value,
                beginDate,
                endDate,
                Status.IMPLEMENTED,
                customer,
                user
        );

        assertEquals(3L, newContract.getId());
        assertEquals("Full Contract", newContract.getName());
        assertEquals("Full content", newContract.getContent());
        assertEquals(value, newContract.getValue());
        assertEquals(beginDate, newContract.getBeginDate());
        assertEquals(endDate, newContract.getEndDate());
        assertEquals(Status.IMPLEMENTED, newContract.getStatus());
        assertEquals(customer, newContract.getCustomer());
        assertEquals(user, newContract.getUser());
    }

    @Test
    @DisplayName("Should handle no args constructor")
    void testNoArgsConstructor() {
        Contract newContract = new Contract();
        assertNotNull(newContract);
        assertNull(newContract.getId());
        assertNull(newContract.getName());
    }
}
