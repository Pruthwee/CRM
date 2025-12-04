package crm.controller;

import crm.entity.Category;
import crm.entity.Customer;
import crm.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Tests")
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
        customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .phone(123456789)
                .firstName("John")
                .lastName("Doe")
                .city("New York")
                .address("123 Main St")
                .enabled(1)
                .categories(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("Should show all customers")
    void testShowAllCustomers() {
        when(customerService.listAllCustomers()).thenReturn(Arrays.asList(customer));

        String viewName = customerController.showAllCustomers(model);

        assertEquals("customer/list", viewName);
        verify(model, times(1)).addAttribute(eq("customers"), any());
        verify(customerService, times(1)).listAllCustomers();
    }

    @Test
    @DisplayName("Should show form to add customer")
    void testShowFormAddCustomer() {
        String viewName = customerController.showFormAddCustomer(model);

        assertEquals("customer/add", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should process add customer request successfully")
    void testProcessRequestAddCustomerSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = customerController.processRequestAddCustomer(customer, bindingResult);

        assertEquals("customer/success", viewName);
        verify(customerService, times(1)).saveCustomer(customer);
    }

    @Test
    @DisplayName("Should redirect when adding customer with errors")
    void testProcessRequestAddCustomerWithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = customerController.processRequestAddCustomer(customer, bindingResult);

        assertEquals("redirect:/customer/add", viewName);
        verify(customerService, never()).saveCustomer(any());
    }

    @Test
    @DisplayName("Should show form to edit customer")
    void testShowFormEditCustomer() {
        when(customerService.showCustomer(1L)).thenReturn(customer);

        String viewName = customerController.showFormEditCustomer(model, 1L);

        assertEquals("customer/edit", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any());
        verify(customerService, times(1)).showCustomer(1L);
    }

    @Test
    @DisplayName("Should process edit customer request successfully")
    void testProcessRequestEditCustomerSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = customerController.processRequestEditCustomer(1L, customer, bindingResult);

        assertEquals("redirect:/customer/list", viewName);
        verify(customerService, times(1)).saveCustomer(customer);
    }

    @Test
    @DisplayName("Should redirect when editing customer with errors")
    void testProcessRequestEditCustomerWithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = customerController.processRequestEditCustomer(1L, customer, bindingResult);

        assertEquals("redirect:/customer/edit/1", viewName);
        verify(customerService, never()).saveCustomer(any());
    }

    @Test
    @DisplayName("Should show name search form")
    void testShowNameSearchForm() {
        String viewName = customerController.showNameSearchForm(model);

        assertEquals("customer/name-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should process name search request")
    void testProcessRequestNameSearch() {
        when(customerService.findOneByEnabledTrueAndName("Test Customer")).thenReturn(customer);

        String viewName = customerController.processRequestNameSearch(customer, model);

        assertEquals("customer/show-one", viewName);
        verify(customerService, times(1)).findOneByEnabledTrueAndName(customer.getName());
    }

    @Test
    @DisplayName("Should show email search form")
    void testShowEmailSearchForm() {
        String viewName = customerController.showEmailSearchForm(model);

        assertEquals("customer/email-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should process email search request")
    void testProcessRequestEmailSearch() {
        when(customerService.findByEnabledTrueAndEmail("test@example.com")).thenReturn(Arrays.asList(customer));

        String viewName = customerController.processRequestEmailSearch(customer, model);

        assertEquals("customer/show-list", viewName);
        verify(customerService, times(1)).findByEnabledTrueAndEmail(customer.getEmail());
    }

    @Test
    @DisplayName("Should show phone search form")
    void testShowPhoneSearchForm() {
        String viewName = customerController.showPhoneSearchForm(model);

        assertEquals("customer/phone-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should process phone search request")
    void testProcessRequestPhoneSearch() {
        when(customerService.findByEnabledTrueAndPhone(123456789)).thenReturn(Arrays.asList(customer));

        String viewName = customerController.processRequestPhoneSearch(customer, model);

        assertEquals("customer/show-list", viewName);
        verify(customerService, times(1)).findByEnabledTrueAndPhone(customer.getPhone());
    }

    @Test
    @DisplayName("Should show city search form")
    void testShowCitySearchForm() {
        String viewName = customerController.showCitySearchForm(model);

        assertEquals("customer/city-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should process city search request")
    void testProcessRequestCitySearch() {
        when(customerService.findByEnabledTrueAndCity("New York")).thenReturn(Arrays.asList(customer));

        String viewName = customerController.processRequestCitySearch(customer, model);

        assertEquals("customer/show-list", viewName);
        verify(customerService, times(1)).findByEnabledTrueAndCity(customer.getCity());
    }

    @Test
    @DisplayName("Should show form to create customer based on another one")
    void testShowFormCreateCustomerBasedOnAnotherOne() {
        when(customerService.showCustomer(1L)).thenReturn(customer);

        String viewName = customerController.showFormCreateCustomerBasedOnAnotherOne(model, 1L);

        assertEquals("customer/add-customer-based-on-another-one", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any());
        verify(customerService, times(1)).showCustomer(1L);
    }

    @Test
    @DisplayName("Should create customer based on another one successfully")
    void testCreateCustomerBasedOnAnotherOneSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(customerService.getMaxId()).thenReturn(10L);

        String viewName = customerController.createCustomerBasedOnAnotherOne(1L, customer, bindingResult);

        assertEquals("redirect:/customer/list", viewName);
        verify(customerService, times(1)).getMaxId();
        verify(customerService, times(1)).saveCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Should handle first name search")
    void testShowFirstNameSearchForm() {
        String viewName = customerController.showFirstNameSearchForm(model);

        assertEquals("customer/first-name-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should handle last name search")
    void testShowLastNameSearchForm() {
        String viewName = customerController.showLastNameSearchForm(model);

        assertEquals("customer/last-name-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should handle city address search")
    void testShowCityAddressSearchForm() {
        String viewName = customerController.showCityAddressSearchForm(model);

        assertEquals("customer/city-address-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }

    @Test
    @DisplayName("Should process first name last name search")
    void testShowFirstNameLastNameSearchForm() {
        String viewName = customerController.showFirstNameLastNameSearchForm(model);

        assertEquals("customer/first-name-last-name-search", viewName);
        verify(model, times(1)).addAttribute(eq("customer"), any(Customer.class));
    }
}
