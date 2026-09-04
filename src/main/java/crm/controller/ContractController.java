package crm.controller;

import crm.entity.Contract;
import crm.entity.Customer;
import crm.entity.User;
import crm.service.ContractService;
import crm.service.CustomerService;
import crm.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/contract")
public class ContractController {

    private ContractService contractService;

    private CustomerService customerService;

    private UserService userService;

    public ContractController(ContractService contractService, CustomerService customerService, UserService userService) {
        this.contractService = contractService;
        this.customerService = customerService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public String showAllContracts(Model model) {
        model.addAttribute("contracts", contractService.listAllContracts());
        return "contract/list";
    }

    @GetMapping("/add")
    public String showFormAddContract(Model model) {
        Iterable<Customer> customers = customerService.findAllByEnabledTrue();
        Iterable<User> users = userService.listAllUsers();
        model.addAttribute("contract", new Contract());
        model.addAttribute("customers", customers);
        model.addAttribute("users", users);
        return "contract/add";
    }

    @PostMapping("/add")
    public String processRequestAddContract(@Valid Contract contract, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/contract/add";
        } else {
            contractService.saveContract(contract);
            return "contract/success";
        }
    }

    @GetMapping("/edit/{id}")
    public String showFormEditContract(Model model, @PathVariable Long id) {
        model.addAttribute("contract", contractService.showContract(id));
        return "contract/edit";
    }

    @PostMapping("/edit/{id}")
    public String processRequestEditContract(@PathVariable Long id, @Valid Contract contract,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/contract/edit/" + id;
        } else {
            contractService.saveContract(contract);
            return "redirect:/contract/list";
        }
    }

    @GetMapping("/name-search")
    public String showNameSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/name-search";
    }

    @PostMapping("/name-search")
    public String processRequestNameSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contract", contractService.findByName(contract.getName()));
        return "contract/show-one";
    }

    @GetMapping("/value-le-search")
    public String showValueLessThanEqualSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/value-le-search";
    }

    @PostMapping("/value-le-search")
    public String processRequestValueLessThanEqualSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByValueLessThanEqual(contract.getValue()));
        return "contract/show-list";
    }

    @GetMapping("/value-ge-search")
    public String showValueGreaterThanEqualSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/value-ge-search";
    }

    @PostMapping("/value-ge-search")
    public String processRequestValueGreaterThanEqualSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByValueGreaterThanEqual(contract.getValue()));
        return "contract/show-list";
    }

    @GetMapping("/begin-date-search")
    public String showBeginDateSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/begin-date-search";
    }

    @PostMapping("/begin-date-search")
    public String processRequestBeginDateSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByBeginDate(contract.getBeginDate()));
        return "contract/show-list";
    }

    @GetMapping("/begin-date-before-search")
    public String showBeginDateBeforeSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/begin-date-before-search";
    }

    @PostMapping("/begin-date-before-search")
    public String processRequestBeginDateBeforeSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByBeginDateBefore(contract.getBeginDate()));
        return "contract/show-list";
    }

    @GetMapping("/begin-date-after-search")
    public String showBeginDateAfterSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/begin-date-after-search";
    }

    @PostMapping("/begin-date-after-search")
    public String processRequestBeginDateAfterSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByBeginDateAfter(contract.getBeginDate()));
        return "contract/show-list";
    }

    @GetMapping("/end-date-search")
    public String showEndDateSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/end-date-search";
    }

    @PostMapping("/end-date-search")
    public String processRequestEndDateSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByEndDate(contract.getEndDate()));
        return "contract/show-list";
    }

    @GetMapping("/end-date-before-search")
    public String showEndDateBeforeSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/end-date-before-search";
    }

    @PostMapping("/end-date-before-search")
    public String processRequestEndDateBeforeSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByEndDateBefore(contract.getEndDate()));
        return "contract/show-list";
    }

    @GetMapping("/end-date-after-search")
    public String showEndDateAfterSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/end-date-after-search";
    }

    @PostMapping("/end-date-after-search")
    public String processRequestEndDateAfterSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByEndDateAfter(contract.getEndDate()));
        return "contract/show-list";
    }

    @GetMapping("/status-search")
    public String showStatusSearchForm(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/status-search";
    }

    @PostMapping("/status-search")
    public String processRequestStatusSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByStatus(contract.getStatus()));
        return "contract/show-list";
    }

    @GetMapping("/customer-search")
    public String showCustomerSearchForm(Model model) {
        Iterable<Customer> customers = customerService.findAllByEnabledTrue();
        model.addAttribute("contract", new Contract());
        model.addAttribute("customers", customers);
        return "contract/customer-search";
    }

    @PostMapping("/customer-search")
    public String processRequestCustomerSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByCustomer(contract.getCustomer()));
        return "contract/show-list";
    }

    @GetMapping("/customer-user-search")
    public String showCustomerUserSearchForm(Model model) {
        Iterable<Customer> customers = customerService.findAllByEnabledTrue();
        Iterable<User> users = userService.listAllUsers();
        model.addAttribute("contract", new Contract());
        model.addAttribute("customers", customers);
        model.addAttribute("users", users);
        return "contract/customer-user-search";
    }

    @PostMapping("/customer-user-search")
    public String processRequestCustomerUserSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByCustomerAndUser(contract.getCustomer(), contract.getUser()));
        return "contract/show-list";
    }

    @GetMapping("/user-search")
    public String showUserSearchForm(Model model) {
        Iterable<User> users = userService.listAllUsers();
        model.addAttribute("contract", new Contract());
        model.addAttribute("users", users);
        return "contract/user-search";
    }

    @PostMapping("/user-search")
    public String processRequestUserSearch(@ModelAttribute Contract contract, Model model) {
        model.addAttribute("contracts", contractService.findAllByUser(contract.getUser()));
        return "contract/show-list";
    }

}
