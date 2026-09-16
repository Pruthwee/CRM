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
        customer = new Customer();
        customer.setId(1L);
        customer.setName("TestCustomer");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        contract = new Contract();
        contract.setId(1L);
        contract.setName("Contract-001");
        contract.setValue(new BigDecimal("1000.00"));
        contract.setBeginDate(LocalDate.of(2024, 1, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));
        contract.setStatus(Status.PROPOSED);
        contract.setCustomer(customer);
        contract.setUser(user);
    }

    @Test
    void testConstructor() {
        ContractController controller = new ContractController(contractService, customerService, userService);
        assertNotNull(controller);
    }

    @Test
    void testShowAllContracts_returnsCorrectView() {
        when(contractService.listAllContracts()).thenReturn(Arrays.asList(contract));
        String view = contractController.showAllContracts(model);
        assertEquals("contract/list", view);
        verify(model).addAttribute(eq("contracts"), any());
    }

    @Test
    void testShowFormAddContract_returnsCorrectView() {
        when(customerService.findAllByEnabledTrue()).thenReturn(Arrays.asList(customer));
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        String view = contractController.showFormAddContract(model);
        assertEquals("contract/add", view);
        verify(model).addAttribute(eq("contract"), any(Contract.class));
        verify(model).addAttribute(eq("customers"), any());
        verify(model).addAttribute(eq("users"), any());
    }

    @Test
    void testProcessRequestAddContract_withErrors_redirectsToAdd() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = contractController.processRequestAddContract(contract, bindingResult);
        assertEquals("redirect:/contract/add", view);
        verify(contractService, never()).saveContract(any());
    }

    @Test
    void testProcessRequestAddContract_noErrors_savesAndReturnsSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = contractController.processRequestAddContract(contract, bindingResult);
        assertEquals("contract/success", view);
        verify(contractService).saveContract(contract);
    }

    @Test
    void testShowFormEditContract_returnsCorrectView() {
        when(contractService.showContract(1L)).thenReturn(contract);
        String view = contractController.showFormEditContract(model, 1L);
        assertEquals("contract/edit", view);
        verify(model).addAttribute(eq("contract"), eq(contract));
    }

    @Test
    void testProcessRequestEditContract_withErrors_redirectsToEdit() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = contractController.processRequestEditContract(1L, contract, bindingResult);
        assertEquals("redirect:/contract/edit/1", view);
        verify(contractService, never()).saveContract(any());
    }

    @Test
    void testProcessRequestEditContract_noErrors_savesAndRedirects() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = contractController.processRequestEditContract(1L, contract, bindingResult);
        assertEquals("redirect:/contract/list", view);
        verify(contractService).saveContract(contract);
    }

    @Test
    void testShowNameSearchForm_returnsCorrectView() {
        String view = contractController.showNameSearchForm(model);
        assertEquals("contract/name-search", view);
        verify(model).addAttribute(eq("contract"), any(Contract.class));
    }

    @Test
    void testProcessRequestNameSearch_returnsCorrectView() {
        when(contractService.findByName("Contract-001")).thenReturn(contract);
        String view = contractController.processRequestNameSearch(contract, model);
        assertEquals("contract/show-one", view);
        verify(model).addAttribute(eq("contract"), eq(contract));
    }

    @Test
    void testShowValueLessThanEqualSearchForm_returnsCorrectView() {
        String view = contractController.showValueLeesThanEqualSearchForm(model);
        assertEquals("contract/value-le-search", view);
    }

    @Test
    void testProcessRequestValueLessThanEqualSearch_returnsCorrectView() {
        when(contractService.findAllByValueLessThanEqual(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestValueLessThanEqualSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowValueGreaterThanEqualSearchForm_returnsCorrectView() {
        String view = contractController.showValueGreaterThanEqualSearchForm(model);
        assertEquals("contract/value-ge-search", view);
    }

    @Test
    void testProcessRequestValueGreaterThanEqualSearch_returnsCorrectView() {
        when(contractService.findAllByValueGreaterThanEqual(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestValueGreaterThanEqualSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowBeginDateSearchForm_returnsCorrectView() {
        String view = contractController.showBeginDateSearchForm(model);
        assertEquals("contract/begin-date-search", view);
    }

    @Test
    void testProcessRequestBeginDateSearch_returnsCorrectView() {
        when(contractService.findAllByBeginDate(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestBeginDateSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowBeginDateBeforeSearchForm_returnsCorrectView() {
        String view = contractController.showBeginDateBeforeSearchForm(model);
        assertEquals("contract/begin-date-before-search", view);
    }

    @Test
    void testProcessRequestBeginDateBeforeSearch_returnsCorrectView() {
        when(contractService.findAllByBeginDateBefore(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestBeginDateBeforeSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowBeginDateAfterSearchForm_returnsCorrectView() {
        String view = contractController.showBeginDateAfterSearchForm(model);
        assertEquals("contract/begin-date-after-search", view);
    }

    @Test
    void testProcessRequestBeginDateAfterSearch_returnsCorrectView() {
        when(contractService.findAllByBeginDateAfter(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestBeginDateAfterSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowEndDateSearchForm_returnsCorrectView() {
        String view = contractController.showEndDateSearchForm(model);
        assertEquals("contract/end-date-search", view);
    }

    @Test
    void testProcessRequestEndDateSearch_returnsCorrectView() {
        when(contractService.findAllByEndDate(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestEndDateSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowEndDateBeforeSearchForm_returnsCorrectView() {
        String view = contractController.showEndDateBeforeSearchForm(model);
        assertEquals("contract/end-date-before-search", view);
    }

    @Test
    void testProcessRequestEndDateBeforeSearch_returnsCorrectView() {
        when(contractService.findAllByEndDateBefore(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestEndDateBeforeSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowEndDateAfterSearchForm_returnsCorrectView() {
        String view = contractController.showEndDateAfterSearchForm(model);
        assertEquals("contract/end-date-after-search", view);
    }

    @Test
    void testProcessRequestEndDateAfterSearch_returnsCorrectView() {
        when(contractService.findAllByEndDateAfter(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestEndDateAfterSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowStatusSearchForm_returnsCorrectView() {
        String view = contractController.showStatusSearchForm(model);
        assertEquals("contract/status-search", view);
    }

    @Test
    void testProcessRequestStatusSearch_returnsCorrectView() {
        when(contractService.findAllByStatus(Status.PROPOSED)).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestStatusSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowCustomerSearchForm_returnsCorrectView() {
        when(customerService.findAllByEnabledTrue()).thenReturn(Arrays.asList(customer));
        String view = contractController.showCustomerSearchForm(model);
        assertEquals("contract/customer-search", view);
    }

    @Test
    void testProcessRequestCustomerSearch_returnsCorrectView() {
        when(contractService.findAllByCustomer(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestCustomerSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowCustomerUserSearchForm_returnsCorrectView() {
        when(customerService.findAllByEnabledTrue()).thenReturn(Arrays.asList(customer));
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        String view = contractController.showCustomerUserSearchForm(model);
        assertEquals("contract/customer-user-search", view);
    }

    @Test
    void testProcessRequestCustomerUserSearch_returnsCorrectView() {
        when(contractService.findAllByCustomerAndUser(any(), any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestCustomerUserSearch(contract, model);
        assertEquals("contract/show-list", view);
    }

    @Test
    void testShowUserSearchForm_returnsCorrectView() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(user));
        String view = contractController.showUserSearchForm(model);
        assertEquals("contract/user-search", view);
    }

    @Test
    void testProcessRequestUserSearch_returnsCorrectView() {
        when(contractService.findAllByUser(any())).thenReturn(Arrays.asList(contract));
        String view = contractController.processRequestUserSearch(contract, model);
        assertEquals("contract/show-list", view);
    }
}
