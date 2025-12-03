package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private CustomerController customerController;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .categories(new HashSet<>())
                .enabled(1)
                .build();
    }

    @Test
    void testConstructor() {
        CustomerService service = mock(CustomerService.class);
        CustomerController controller = new CustomerController(service);
        assertNotNull(controller);
    }

    @Test
    void testShowAllCustomers() {
        when(customerService.listAllCustomers()).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.showAllCustomers(model);

        assertEquals("customer/list", result);
        verify(model).addAttribute("customers", Arrays.asList(testCustomer));
    }

    @Test
    void testShowFormAddCustomer() {
        String result = customerController.showFormAddCustomer(model);

        assertEquals("customer/add", result);
        verify(model).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    void testProcessRequestAddCustomerSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = customerController.processRequestAddCustomer(testCustomer, bindingResult);

        assertEquals("customer/success", result);
        verify(customerService).saveCustomer(testCustomer);
    }

    @Test
    void testProcessRequestAddCustomerValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = customerController.processRequestAddCustomer(testCustomer, bindingResult);

        assertEquals("redirect:/customer/add", result);
        verify(customerService, never()).saveCustomer(any());
    }

    @Test
    void testShowFormEditCustomer() {
        when(customerService.showCustomer(1L)).thenReturn(testCustomer);

        String result = customerController.showFormEditCustomer(model, 1L);

        assertEquals("customer/edit", result);
        verify(model).addAttribute("customer", testCustomer);
    }

    @Test
    void testProcessRequestEditCustomerSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = customerController.processRequestEditCustomer(1L, testCustomer, bindingResult);

        assertEquals("redirect:/customer/list", result);
        verify(customerService).saveCustomer(testCustomer);
    }

    @Test
    void testProcessRequestEditCustomerValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = customerController.processRequestEditCustomer(1L, testCustomer, bindingResult);

        assertEquals("redirect:/customer/edit/1", result);
        verify(customerService, never()).saveCustomer(any());
    }

    @Test
    void testShowFormCreateCustomerBasedOnAnotherOne() {
        when(customerService.showCustomer(1L)).thenReturn(testCustomer);

        String result = customerController.showFormCreateCustomerBasedOnAnotherOne(model, 1L);

        assertEquals("customer/add-customer-based-on-another-one", result);
        verify(model).addAttribute("customer", testCustomer);
    }

    @Test
    void testCreateCustomerBasedOnAnotherOneSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(customerService.getMaxId()).thenReturn(10L);

        String result = customerController.createCustomerBasedOnAnotherOne(1L, testCustomer, bindingResult);

        assertEquals("redirect:/customer/list", result);
        verify(customerService).saveCustomer(any(Customer.class));
    }

    @Test
    void testShowNameSearchForm() {
        String result = customerController.showNameSearchForm(model);

        assertEquals("customer/name-search", result);
        verify(model).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    void testProcessRequestNameSearch() {
        when(customerService.findOneByEnabledTrueAndName("Test Customer")).thenReturn(testCustomer);

        String result = customerController.processRequestNameSearch(testCustomer, model);

        assertEquals("customer/show-one", result);
        verify(model).addAttribute("customer", testCustomer);
    }

    @Test
    void testShowEmailSearchForm() {
        String result = customerController.showEmailSearchForm(model);

        assertEquals("customer/email-search", result);
    }

    @Test
    void testProcessRequestEmailSearch() {
        when(customerService.findByEnabledTrueAndEmail("test@example.com")).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.processRequestEmailSearch(testCustomer, model);

        assertEquals("customer/show-list", result);
    }

    @Test
    void testShowPhoneSearchForm() {
        String result = customerController.showPhoneSearchForm(model);

        assertEquals("customer/phone-search", result);
    }

    @Test
    void testProcessRequestPhoneSearch() {
        when(customerService.findByEnabledTrueAndPhone(123456789)).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.processRequestPhoneSearch(testCustomer, model);

        assertEquals("customer/show-list", result);
    }

    @Test
    void testShowFirstNameSearchForm() {
        String result = customerController.showFirstNameSearchForm(model);

        assertEquals("customer/first-name-search", result);
    }

    @Test
    void testProcessRequestFirstNameSearch() {
        when(customerService.findByEnabledTrueAndFirstName("John")).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.processRequestFirstNameSearch(testCustomer, model);

        assertEquals("customer/show-list", result);
    }

    @Test
    void testShowLastNameSearchForm() {
        String result = customerController.showLastNameSearchForm(model);

        assertEquals("customer/last-name-search", result);
    }

    @Test
    void testProcessRequestLastNameSearch() {
        when(customerService.findByEnabledTrueAndLastName("Doe")).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.processRequestLastNameSearch(testCustomer, model);

        assertEquals("customer/show-list", result);
    }

    @Test
    void testShowFirstNameLastNameSearchForm() {
        String result = customerController.showFirstNameLastNameSearchForm(model);

        assertEquals("customer/first-name-last-name-search", result);
    }

    @Test
    void testProcessRequestFirstNameLastNameSearch() {
        when(customerService.findByEnabledTrueAndFirstNameAndLastName("John", "Doe")).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.processRequestFirstNameLastNameSearch(testCustomer, model);

        assertEquals("customer/show-list", result);
    }

    @Test
    void testShowCitySearchForm() {
        String result = customerController.showCitySearchForm(model);

        assertEquals("customer/city-search", result);
    }

    @Test
    void testProcessRequestCitySearch() {
        when(customerService.findByEnabledTrueAndCity("New York")).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.processRequestCitySearch(testCustomer, model);

        assertEquals("customer/show-list", result);
    }

    @Test
    void testShowCityAddressSearchForm() {
        String result = customerController.showCityAddressSearchForm(model);

        assertEquals("customer/city-address-search", result);
    }

    @Test
    void testProcessRequestCityAddressSearch() {
        when(customerService.findByEnabledTrueAndCityAndAddress("New York", "123 Main St")).thenReturn(Arrays.asList(testCustomer));

        String result = customerController.processRequestCityAddressSearch(testCustomer, model);

        assertEquals("customer/show-list", result);
    }
}
