package crm.controller;

import crm.entity.Contract;
import crm.entity.Customer;
import crm.entity.Status;
import crm.entity.User;
import crm.service.ContractService;
import crm.service.CustomerService;
import crm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractController Tests")
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
        customer = Customer.builder().id(1L).name("Test Customer").build();
        user = User.builder().id(1L).username("testuser").build();

        contract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .value(new BigDecimal("10000.00"))
                .beginDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status(Status.PROPOSED)
                .customer(customer)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Should show all contracts")
    void testShowAllContracts() {
        when(contractService.listAllContracts()).thenReturn(Arrays.asList(contract));

        String viewName = contractController.showAllContracts(model);

        assertEquals("contract/list", viewName);
        verify(model, times(1)).addAttribute(eq("contracts"), any());
        verify(contractService, times(1)).listAllContracts();
    }

    @Test
    @DisplayName("Should show form to add contract")
    void testShowFormAddContract() {
        when(customerService.findAllByEnabledTrue()).thenReturn(Arrays.asList(customer));
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));

        String viewName = contractController.showFormAddContract(model);

        assertEquals("contract/add", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any(Contract.class));
        verify(model, times(1)).addAttribute(eq("customers"), any());
        verify(model, times(1)).addAttribute(eq("users"), any());
    }

    @Test
    @DisplayName("Should process add contract successfully")
    void testProcessRequestAddContractSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = contractController.processRequestAddContract(contract, bindingResult);

        assertEquals("contract/success", viewName);
        verify(contractService, times(1)).saveContract(contract);
    }

    @Test
    @DisplayName("Should redirect when adding contract with errors")
    void testProcessRequestAddContractWithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = contractController.processRequestAddContract(contract, bindingResult);

        assertEquals("redirect:/contract/add", viewName);
        verify(contractService, never()).saveContract(any());
    }

    @Test
    @DisplayName("Should show form to edit contract")
    void testShowFormEditContract() {
        when(contractService.showContract(1L)).thenReturn(contract);

        String viewName = contractController.showFormEditContract(model, 1L);

        assertEquals("contract/edit", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any());
        verify(contractService, times(1)).showContract(1L);
    }

    @Test
    @DisplayName("Should process edit contract successfully")
    void testProcessRequestEditContractSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = contractController.processRequestEditContract(1L, contract, bindingResult);

        assertEquals("redirect:/contract/list", viewName);
        verify(contractService, times(1)).saveContract(contract);
    }

    @Test
    @DisplayName("Should show name search form")
    void testShowNameSearchForm() {
        String viewName = contractController.showNameSearchForm(model);

        assertEquals("contract/name-search", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any(Contract.class));
    }

    @Test
    @DisplayName("Should process name search")
    void testProcessRequestNameSearch() {
        when(contractService.findByName("Test Contract")).thenReturn(contract);

        String viewName = contractController.processRequestNameSearch(contract, model);

        assertEquals("contract/show-one", viewName);
        verify(contractService, times(1)).findByName(contract.getName());
    }

    @Test
    @DisplayName("Should show value less than equal search form")
    void testShowValueLessThanEqualSearchForm() {
        String viewName = contractController.showValueLeesThanEqualSearchForm(model);

        assertEquals("contract/value-le-search", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any(Contract.class));
    }

    @Test
    @DisplayName("Should show begin date search form")
    void testShowBeginDateSearchForm() {
        String viewName = contractController.showBeginDateSearchForm(model);

        assertEquals("contract/begin-date-search", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any(Contract.class));
    }

    @Test
    @DisplayName("Should show status search form")
    void testShowStatusSearchForm() {
        String viewName = contractController.showStatusSearchForm(model);

        assertEquals("contract/status-search", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any(Contract.class));
    }

    @Test
    @DisplayName("Should show customer search form")
    void testShowCustomerSearchForm() {
        when(customerService.findAllByEnabledTrue()).thenReturn(Arrays.asList(customer));

        String viewName = contractController.showCustomerSearchForm(model);

        assertEquals("contract/customer-search", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any(Contract.class));
        verify(model, times(1)).addAttribute(eq("customers"), any());
    }

    @Test
    @DisplayName("Should show user search form")
    void testShowUserSearchForm() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));

        String viewName = contractController.showUserSearchForm(model);

        assertEquals("contract/user-search", viewName);
        verify(model, times(1)).addAttribute(eq("contract"), any(Contract.class));
        verify(model, times(1)).addAttribute(eq("users"), any());
    }

    @Test
    @DisplayName("Should process value greater than equal search")
    void testProcessRequestValueGreaterThanEqualSearch() {
        when(contractService.findAllByValueGreaterThanEqual(any())).thenReturn(Arrays.asList(contract));

        String viewName = contractController.processRequestValueGreaterThanEqualSearch(contract, model);

        assertEquals("contract/show-list", viewName);
        verify(contractService, times(1)).findAllByValueGreaterThanEqual(any());
    }

    @Test
    @DisplayName("Should process begin date search")
    void testProcessRequestBeginDateSearch() {
        when(contractService.findAllByBeginDate(any())).thenReturn(Arrays.asList(contract));

        String viewName = contractController.processRequestBeginDateSearch(contract, model);

        assertEquals("contract/show-list", viewName);
        verify(contractService, times(1)).findAllByBeginDate(any());
    }

    @Test
    @DisplayName("Should process status search")
    void testProcessRequestStatusSearch() {
        when(contractService.findAllByStatus(any())).thenReturn(Arrays.asList(contract));

        String viewName = contractController.processRequestStatusSearch(contract, model);

        assertEquals("contract/show-list", viewName);
        verify(contractService, times(1)).findAllByStatus(any());
    }
}
