package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private CustomerController customerController;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("TestCo");
        customer.setEmail("test@test.com");
        customer.setPhone(123456789);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCity("NYC");
        customer.setAddress("123 Main St");
        customer.setEnabled(1);
    }

    @Test
    void testConstructor() {
        CustomerController controller = new CustomerController(customerService);
        assertNotNull(controller);
    }

    @Test
    void testShowAllCustomers_returnsCorrectView() {
        when(customerService.listAllCustomers()).thenReturn(Arrays.asList(customer));
        String view = customerController.showAllCustomers(model);
        assertEquals("customer/list", view);
        verify(model).addAttribute(eq("customers"), any());
    }

    @Test
    void testShowFormAddCustomer_returnsCorrectView() {
        String view = customerController.showFormAddCustomer(model);
        assertEquals("customer/add", view);
        verify(model).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    void testProcessRequestAddCustomer_withErrors_redirectsToAdd() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = customerController.processRequestAddCustomer(customer, bindingResult);
        assertEquals("redirect:/customer/add", view);
        verify(customerService, never()).saveCustomer(any());
    }

    @Test
    void testProcessRequestAddCustomer_noErrors_savesAndReturnsSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = customerController.processRequestAddCustomer(customer, bindingResult);
        assertEquals("customer/success", view);
        verify(customerService).saveCustomer(customer);
    }

    @Test
    void testShowFormEditCustomer_returnsCorrectView() {
        when(customerService.showCustomer(1L)).thenReturn(customer);
        String view = customerController.showFormEditCustomer(model, 1L);
        assertEquals("customer/edit", view);
        verify(model).addAttribute(eq("customer"), eq(customer));
    }

    @Test
    void testProcessRequestEditCustomer_withErrors_redirectsToEdit() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = customerController.processRequestEditCustomer(1L, customer, bindingResult);
        assertEquals("redirect:/customer/edit/1", view);
        verify(customerService, never()).saveCustomer(any());
    }

    @Test
    void testProcessRequestEditCustomer_noErrors_savesAndRedirects() {
        when(bindingResult.hasErrors()).thenReturn(false);
        String view = customerController.processRequestEditCustomer(1L, customer, bindingResult);
        assertEquals("redirect:/customer/list", view);
        verify(customerService).saveCustomer(customer);
    }

    @Test
    void testShowFormCreateCustomerBasedOnAnotherOne_returnsCorrectView() {
        when(customerService.showCustomer(1L)).thenReturn(customer);
        String view = customerController.showFormCreateCustomerBasedOnAnotherOne(model, 1L);
        assertEquals("customer/add-customer-based-on-another-one", view);
        verify(model).addAttribute(eq("customer"), eq(customer));
    }

    @Test
    void testCreateCustomerBasedOnAnotherOne_withErrors_redirects() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = customerController.createCustomerBasedOnAnotherOne(1L, customer, bindingResult);
        assertEquals("redirect:/customer/addCustomerBasedOnAnotherOne/1", view);
    }

    @Test
    void testCreateCustomerBasedOnAnotherOne_noErrors_savesAndRedirects() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(customerService.getMaxId()).thenReturn(5L);
        String view = customerController.createCustomerBasedOnAnotherOne(1L, customer, bindingResult);
        assertEquals("redirect:/customer/list", view);
        verify(customerService).saveCustomer(any(Customer.class));
    }

    @Test
    void testShowNameSearchForm_returnsCorrectView() {
        String view = customerController.showNameSearchForm(model);
        assertEquals("customer/name-search", view);
        verify(model).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    void testProcessRequestNameSearch_returnsCorrectView() {
        when(customerService.findOneByEnabledTrueAndName("TestCo")).thenReturn(customer);
        String view = customerController.processRequestNameSearch(customer, model);
        assertEquals("customer/show-one", view);
        verify(model).addAttribute(eq("customer"), eq(customer));
    }

    @Test
    void testShowEmailSearchForm_returnsCorrectView() {
        String view = customerController.showEmailSearchForm(model);
        assertEquals("customer/email-search", view);
    }

    @Test
    void testProcessRequestEmailSearch_returnsCorrectView() {
        when(customerService.findByEnabledTrueAndEmail("test@test.com")).thenReturn(Arrays.asList(customer));
        String view = customerController.processRequestEmailSearch(customer, model);
        assertEquals("customer/show-list", view);
        verify(model).addAttribute(eq("customers"), any());
    }

    @Test
    void testShowPhoneSearchForm_returnsCorrectView() {
        String view = customerController.showPhoneSearchForm(model);
        assertEquals("customer/phone-search", view);
    }

    @Test
    void testProcessRequestPhoneSearch_returnsCorrectView() {
        when(customerService.findByEnabledTrueAndPhone(123456789)).thenReturn(Arrays.asList(customer));
        String view = customerController.processRequestPhoneSearch(customer, model);
        assertEquals("customer/show-list", view);
        verify(model).addAttribute(eq("customers"), any());
    }

    @Test
    void testShowFirstNameSearchForm_returnsCorrectView() {
        String view = customerController.showFirstNameSearchForm(model);
        assertEquals("customer/first-name-search", view);
    }

    @Test
    void testProcessRequestFirstNameSearch_returnsCorrectView() {
        when(customerService.findByEnabledTrueAndFirstName("John")).thenReturn(Arrays.asList(customer));
        String view = customerController.processRequestFirstNameSearch(customer, model);
        assertEquals("customer/show-list", view);
    }

    @Test
    void testShowLastNameSearchForm_returnsCorrectView() {
        String view = customerController.showLastNameSearchForm(model);
        assertEquals("customer/last-name-search", view);
    }

    @Test
    void testProcessRequestLastNameSearch_returnsCorrectView() {
        when(customerService.findByEnabledTrueAndLastName("Doe")).thenReturn(Arrays.asList(customer));
        String view = customerController.processRequestLastNameSearch(customer, model);
        assertEquals("customer/show-list", view);
    }

    @Test
    void testShowFirstNameLastNameSearchForm_returnsCorrectView() {
        String view = customerController.showFirstNameLastNameSearchForm(model);
        assertEquals("customer/first-name-last-name-search", view);
    }

    @Test
    void testProcessRequestFirstNameLastNameSearch_returnsCorrectView() {
        when(customerService.findByEnabledTrueAndFirstNameAndLastName("John", "Doe")).thenReturn(Arrays.asList(customer));
        String view = customerController.processRequestFirstNameLastNameSearch(customer, model);
        assertEquals("customer/show-list", view);
    }

    @Test
    void testShowCitySearchForm_returnsCorrectView() {
        String view = customerController.showCitySearchForm(model);
        assertEquals("customer/city-search", view);
    }

    @Test
    void testProcessRequestCitySearch_returnsCorrectView() {
        when(customerService.findByEnabledTrueAndCity("NYC")).thenReturn(Arrays.asList(customer));
        String view = customerController.processRequestCitySearch(customer, model);
        assertEquals("customer/show-list", view);
    }

    @Test
    void testShowCityAddressSearchForm_returnsCorrectView() {
        String view = customerController.showCityAddressSearchForm(model);
        assertEquals("customer/city-address-search", view);
    }

    @Test
    void testProcessRequestCityAddressSearch_returnsCorrectView() {
        when(customerService.findByEnabledTrueAndCityAndAddress("NYC", "123 Main St")).thenReturn(Arrays.asList(customer));
        String view = customerController.processRequestCityAddressSearch(customer, model);
        assertEquals("customer/show-list", view);
    }
}
