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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private Contract testContract;
    private Customer testCustomer;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testContract = Contract.builder()
                .id(1L)
                .name("Test Contract")
                .value(new BigDecimal("10000.00"))
                .beginDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .status(Status.PROPOSED)
                .customer(testCustomer)
                .user(testUser)
                .build();
    }

    @Test
    void testConstructor() {
        ContractService cService = mock(ContractService.class);
        CustomerService cuService = mock(CustomerService.class);
        UserService uService = mock(UserService.class);
        ContractController controller = new ContractController(cService, cuService, uService);
        assertNotNull(controller);
    }

    @Test
    void testShowAllContracts() {
        when(contractService.listAllContracts()).thenReturn(Arrays.asList(testContract));

        String result = contractController.showAllContracts(model);

        assertEquals("contract/list", result);
        verify(model).addAttribute("contracts", Arrays.asList(testContract));
    }

    @Test
    void testShowFormAddContract() {
        when(customerService.findAllByEnabledTrue()).thenReturn(Arrays.asList(testCustomer));
        when(userService.listAllUsers()).thenReturn(Arrays.asList(testUser));

        String result = contractController.showFormAddContract(model);

        assertEquals("contract/add", result);
        verify(model).addAttribute(eq("contract"), any(Contract.class));
        verify(model).addAttribute("customers", Arrays.asList(testCustomer));
        verify(model).addAttribute("users", Arrays.asList(testUser));
    }

    @Test
    void testProcessRequestAddContractSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = contractController.processRequestAddContract(testContract, bindingResult);

        assertEquals("contract/success", result);
        verify(contractService).saveContract(testContract);
    }

    @Test
    void testProcessRequestAddContractValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = contractController.processRequestAddContract(testContract, bindingResult);

        assertEquals("redirect:/contract/add", result);
        verify(contractService, never()).saveContract(any());
    }

    @Test
    void testShowFormEditContract() {
        when(contractService.showContract(1L)).thenReturn(testContract);

        String result = contractController.showFormEditContract(model, 1L);

        assertEquals("contract/edit", result);
        verify(model).addAttribute("contract", testContract);
    }

    @Test
    void testProcessRequestEditContractSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = contractController.processRequestEditContract(1L, testContract, bindingResult);

        assertEquals("redirect:/contract/list", result);
        verify(contractService).saveContract(testContract);
    }

    @Test
    void testShowNameSearchForm() {
        String result = contractController.showNameSearchForm(model);

        assertEquals("contract/name-search", result);
        verify(model).addAttribute(eq("contract"), any(Contract.class));
    }

    @Test
    void testProcessRequestNameSearch() {
        when(contractService.findByName("Test Contract")).thenReturn(testContract);

        String result = contractController.processRequestNameSearch(testContract, model);

        assertEquals("contract/show-one", result);
        verify(model).addAttribute("contract", testContract);
    }

    @Test
    void testShowValueLeesThanEqualSearchForm() {
        String result = contractController.showValueLeesThanEqualSearchForm(model);

        assertEquals("contract/value-le-search", result);
    }

    @Test
    void testProcessRequestValueLessThanEqualSearch() {
        when(contractService.findAllByValueLessThanEqual(testContract.getValue())).thenReturn(Arrays.asList(testContract));

        String result = contractController.processRequestValueLessThanEqualSearch(testContract, model);

        assertEquals("contract/show-list", result);
    }

    @Test
    void testShowValueGreaterThanEqualSearchForm() {
        String result = contractController.showValueGreaterThanEqualSearchForm(model);

        assertEquals("contract/value-ge-search", result);
    }

    @Test
    void testProcessRequestValueGreaterThanEqualSearch() {
        when(contractService.findAllByValueGreaterThanEqual(testContract.getValue())).thenReturn(Arrays.asList(testContract));

        String result = contractController.processRequestValueGreaterThanEqualSearch(testContract, model);

        assertEquals("contract/show-list", result);
    }

    @Test
    void testShowBeginDateSearchForm() {
        String result = contractController.showBeginDateSearchForm(model);

        assertEquals("contract/begin-date-search", result);
    }

    @Test
    void testProcessRequestBeginDateSearch() {
        when(contractService.findAllByBeginDate(testContract.getBeginDate())).thenReturn(Arrays.asList(testContract));

        String result = contractController.processRequestBeginDateSearch(testContract, model);

        assertEquals("contract/show-list", result);
    }

    @Test
    void testShowStatusSearchForm() {
        String result = contractController.showStatusSearchForm(model);

        assertEquals("contract/status-search", result);
    }

    @Test
    void testProcessRequestStatusSearch() {
        when(contractService.findAllByStatus(Status.PROPOSED)).thenReturn(Arrays.asList(testContract));

        String result = contractController.processRequestStatusSearch(testContract, model);

        assertEquals("contract/show-list", result);
    }

    @Test
    void testShowCustomerSearchForm() {
        when(customerService.findAllByEnabledTrue()).thenReturn(Arrays.asList(testCustomer));

        String result = contractController.showCustomerSearchForm(model);

        assertEquals("contract/customer-search", result);
    }

    @Test
    void testProcessRequestCustomerSearch() {
        when(contractService.findAllByCustomer(testContract.getCustomer())).thenReturn(Arrays.asList(testContract));

        String result = contractController.processRequestCustomerSearch(testContract, model);

        assertEquals("contract/show-list", result);
    }

    @Test
    void testShowUserSearchForm() {
        when(userService.listAllUsers()).thenReturn(Arrays.asList(testUser));

        String result = contractController.showUserSearchForm(model);

        assertEquals("contract/user-search", result);
    }

    @Test
    void testProcessRequestUserSearch() {
        when(contractService.findAllByUser(testContract.getUser())).thenReturn(Arrays.asList(testContract));

        String result = contractController.processRequestUserSearch(testContract, model);

        assertEquals("contract/show-list", result);
    }
}
