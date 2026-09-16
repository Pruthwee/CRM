package crm.controller;

import crm.entity.*;
import crm.service.ContractService;
import crm.service.CustomerService;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    @Mock
    private ContractService contractService;

    @Mock
    private CustomerService customerService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ContractController contractController;

    private Contract contract;
    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        customer = Customer.builder()
                .id(1L).name("TestCo").email("test@test.com").enabled(1).build();

        user = User.builder()
                .id(1L).username("testuser").email("user@test.com").role(role).build();

        contract = Contract.builder()
                .id(1L).name("Contract1").content("Content")
                .value(new BigDecimal("1000.00"))
                .beginDate(LocalDate.of(2023, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .status(Status.PROPOSED)
                .customer(customer).user(user).build();
    }

    @Test
    void testShowAllContracts() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.listAllContracts()).thenReturn(contracts);
        String view = contractController.showAllContracts(model);
        assertEquals("contract/list", view);
        verify(model).addAttribute(eq("contracts"), any());
    }

    @Test
    void testShowFormAddContract() {
        List<Customer> customers = Collections.singletonList(customer);
        List<User> users = Collections.singletonList(user);
        when(customerService.findAllByEnabledTrue()).thenReturn(customers);
        when(userService.listAllUsers()).thenReturn(users);
        String view = contractController.showFormAddContract(model);
        assertEquals("contract/add", view);
        verify(model).addAttribute(eq("contract"), any(Contract.class));
        verify(model).addAttribute(eq("customers"), any());
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testProcessRequestAddContract_NoErrors() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = contractController.processRequestAddContract(contract, bindingResult);
        assertEquals("contract/success", view);
        verify(contractService).saveContract(contract);
    }

    @Test
    void testProcessRequestAddContract_WithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = contractController.processRequestAddContract(contract, bindingResult);
        assertEquals("redirect:/contract/add", view);
        verify(contractService, never()).saveContract(any());
    }

    @Test
    void testShowFormEditContract() {
        when(contractService.showContract(1L)).thenReturn(contract);
        String view = contractController.showFormEditContract(model, 1L);
        assertEquals("contract/edit", view);
        verify(model).addAttribute(eq("contract"), eq(contract));
    }

    @Test
    void testProcessRequestEditContract_NoErrors() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = contractController.processRequestEditContract(1L, contract, bindingResult);
        assertEquals("redirect:/contract/list", view);
        verify(contractService).saveContract(contract);
    }

    @Test
    void testProcessRequestEditContract_WithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = contractController.processRequestEditContract(1L, contract, bindingResult);
        assertEquals("redirect:/contract/edit/1", view);
        verify(contractService, never()).saveContract(any());
    }

    @Test
    void testShowNameSearchForm() {
        String view = contractController.showNameSearchForm(model);
        assertEquals("contract/name-search", view);
        verify(model).addAttribute(eq("contract"), any(Contract.class));
    }

    @Test
    void testProcessRequestNameSearch() {
        when(contractService.findByName("Contract1")).thenReturn(contract);
        String view = contractController.processRequestNameSearch(contract, model);
        assertEquals("contract/show-one", view);
        verify(model).addAttribute(eq("contract"), eq(contract));
    }

    @Test
    void testShowValueLessThanEqualSearchForm() {
        String view = contractController.showValueLeesThanEqualSearchForm(model);
        assertEquals("contract/value-le-search", view);
    }

    @Test
    void testProcessRequestValueLessThanEqualSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByValueLessThanEqual(any())).thenReturn(contracts);
        String view = contractController.processRequestValueLessThanEqualSearch(contract, model);
        assertEquals("contract/show-list", view);
        verify(model).addAttribute(eq("contracts"), any());
    }

    @Test
    void testShowValueGreaterThanEqualSearchForm() {
        String view = contractController.showValueGreaterThanEqualSearchForm(model);
        assertEquals("contract/value-ge-search", view);
    }

    @Test
    void testProcessRequestValueGreaterThanEqualSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByValueGreaterThanEqual(any())).thenReturn(contracts);
        String view = contractController.processRequestValueGreaterThanEqualSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowBeginDateSearchForm() {
        String view = contractController.showBeginDateSearchForm(model);
        assertEquals("contract/begin-date-search", view);
    }

    @Test
    void testProcessRequestBeginDateSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByBeginDate(any())).thenReturn(contracts);
        String view = contractController.processRequestBeginDateSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowBeginDateBeforeSearchForm() {
        String view = contractController.showBeginDateBeforeSearchForm(model);
        assertEquals("contract/begin-date-before-search", view);
    }

    @Test
    void testProcessRequestBeginDateBeforeSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByBeginDateBefore(any())).thenReturn(contracts);
        String view = contractController.processRequestBeginDateBeforeSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowBeginDateAfterSearchForm() {
        String view = contractController.showBeginDateAfterSearchForm(model);
        assertEquals("contract/begin-date-after-search", view);
    }

    @Test
    void testProcessRequestBeginDateAfterSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByBeginDateAfter(any())).thenReturn(contracts);
        String view = contractController.processRequestBeginDateAfterSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowEndDateSearchForm() {
        String view = contractController.showEndDateSearchForm(model);
        assertEquals("contract/end-date-search", view);
    }

    @Test
    void testProcessRequestEndDateSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByEndDate(any())).thenReturn(contracts);
        String view = contractController.processRequestEndDateSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowEndDateBeforeSearchForm() {
        String view = contractController.showEndDateBeforeSearchForm(model);
        assertEquals("contract/end-date-before-search", view);
    }

    @Test
    void testProcessRequestEndDateBeforeSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByEndDateBefore(any())).thenReturn(contracts);
        String view = contractController.processRequestEndDateBeforeSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowEndDateAfterSearchForm() {
        String view = contractController.showEndDateAfterSearchForm(model);
        assertEquals("contract/end-date-after-search", view);
    }

    @Test
    void testProcessRequestEndDateAfterSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByEndDateAfter(any())).thenReturn(contracts);
        String view = contractController.processRequestEndDateAfterSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowStatusSearchForm() {
        String view = contractController.showStatusSearchForm(model);
        assertEquals("contract/status-search", view);
    }

    @Test
    void testProcessRequestStatusSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByStatus(Status.PROPOSED)).thenReturn(contracts);
        String view = contractController.processRequestStatusSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowCustomerSearchForm() {
        List<Customer> customers = Collections.singletonList(customer);
        when(customerService.findAllByEnabledTrue()).thenReturn(customers);
        String view = contractController.showCustomerSearchForm(model);
        assertEquals("contract/customer-search", view);
        verify(model).addAttribute(eq("customers"), any());
    }

    @Test
    void testProcessRequestCustomerSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByCustomer(any())).thenReturn(contracts);
        String view = contractController.processRequestCustomerSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowCustomerUserSearchForm() {
        List<Customer> customers = Collections.singletonList(customer);
        List<User> users = Collections.singletonList(user);
        when(customerService.findAllByEnabledTrue()).thenReturn(customers);
        when(userService.listAllUsers()).thenReturn(users);
        String view = contractController.showCustomerUserSearchForm(model);
        assertEquals("contract/customer-user-search", view);
    }

    @Test
    void testProcessRequestCustomerUserSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByCustomerAndUser(any(), any())).thenReturn(contracts);
        String view = contractController.processRequestCustomerUserSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowUserSearchForm() {
        List<User> users = Collections.singletonList(user);
        when(userService.listAllUsers()).thenReturn(users);
        String view = contractController.showUserSearchForm(model);
        assertEquals("contract/user-search", view);
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testProcessRequestUserSearch() {
        List<Contract> contracts = Collections.singletonList(contract);
        when(contractService.findAllByUser(any())).thenReturn(contracts);
        String view = contractController.processRequestUserSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testConstructorWithServices() {
        ContractController controller = new ContractController(contractService, customerService, userService);
        assertNotNull(controller);
    }
}
