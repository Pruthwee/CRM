package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/list")
    public String showAllCustomers(Model model) {
        model.addAttribute("customers", customerService.listAllCustomers());
        return "customer/list";
    }

    @GetMapping("/add")
    public String showFormAddCustomer(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/add";
    }

    @PostMapping("/add")
    public String processRequestAddCustomer(@Valid Customer customer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/customer/add";
        } else {
            customerService.saveCustomer(customer);
            return "customer/success";
        }
    }

    @GetMapping("/edit/{id}")
    public String showFormEditCustomer(Model model, @PathVariable Long id) {
        model.addAttribute("customer", customerService.showCustomer(id));
        return "customer/edit";
    }

    @PostMapping("/edit/{id}")
    public String processRequestEditCustomer(@PathVariable Long id, @Valid Customer customer,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/customer/edit/" + id;
        } else {
            customerService.saveCustomer(customer);
            return "redirect:/customer/list";
        }
    }

    @GetMapping("/addCustomerBasedOnAnotherOne/{id}")
    public String showFormCreateCustomerBasedOnAnotherOne(Model model, @PathVariable Long id) {
        model.addAttribute("customer", customerService.showCustomer(id));
        return "customer/add-customer-based-on-another-one";
    }

    @PostMapping("/addCustomerBasedOnAnotherOne/{id}")
    public String createCustomerBasedOnAnotherOne(@PathVariable Long id, @Valid Customer customer,
                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/customer/addCustomerBasedOnAnotherOne/" + id;
        } else {
            Customer newCustomer = new Customer(
                    customerService.getMaxId() + 1L,
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getCategories(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getCity(),
                    customer.getAddress(),
                    customer.getEnabled());
            customerService.saveCustomer(newCustomer);
            return "redirect:/customer/list";
        }
    }

    @GetMapping("/name-search")
    public String showNameSearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/name-search";
    }

    @PostMapping("/name-search")
    public String processRequestNameSearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customer", customerService.findOneByEnabledTrueAndName(customer.getName()));
        return "customer/show-one";
    }

    @GetMapping("/email-search")
    public String showEmailSearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/email-search";
    }

    @PostMapping("/email-search")
    public String processRequestEmailSearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customers", customerService.findByEnabledTrueAndEmail(customer.getEmail()));
        return "customer/show-list";
    }

    @GetMapping("/phone-search")
    public String showPhoneSearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/phone-search";
    }

    @PostMapping("/phone-search")
    public String processRequestPhoneSearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customers", customerService.findByEnabledTrueAndPhone(customer.getPhone()));
        return "customer/show-list";
    }

    @GetMapping("/first-name-search")
    public String showFirstNameSearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/first-name-search";
    }

    @PostMapping("/first-name-search")
    public String processRequestFirstNameSearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customers", customerService.findByEnabledTrueAndFirstName(customer.getFirstName()));
        return "customer/show-list";
    }

    @GetMapping("/last-name-search")
    public String showLastNameSearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/last-name-search";
    }

    @PostMapping("/last-name-search")
    public String processRequestLastNameSearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customers", customerService.findByEnabledTrueAndLastName(customer.getLastName()));
        return "customer/show-list";
    }

    @GetMapping("/first-name-last-name-search")
    public String showFirstNameLastNameSearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/first-name-last-name-search";
    }

    @PostMapping("/first-name-last-name-search")
    public String processRequestFirstNameLastNameSearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customers",
                customerService.findByEnabledTrueAndFirstNameAndLastName(customer.getFirstName(), customer.getLastName()));
        return "customer/show-list";
    }

    @GetMapping("/city-search")
    public String showCitySearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/city-search";
    }

    @PostMapping("/city-search")
    public String processRequestCitySearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customers", customerService.findByEnabledTrueAndCity(customer.getCity()));
        return "customer/show-list";
    }

    @GetMapping("/city-address-search")
    public String showCityAddressSearchForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/city-address-search";
    }

    @PostMapping("/city-address-search")
    public String processRequestCityAddressSearch(@ModelAttribute Customer customer, Model model) {
        model.addAttribute("customers",
                customerService.findByEnabledTrueAndCityAndAddress(customer.getCity(), customer.getAddress()));
        return "customer/show-list";
    }

}
