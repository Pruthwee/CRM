package crm.controller;

import crm.entity.User;
import crm.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloud-ready Registration Controller
 * - REST API endpoints for cloud load balancers
 * - Stateless operations
 */
@Controller
public class RegisterController {

    private UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model, User user){
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register")
    public String processRegistrationForm(Model model, @Valid User user, BindingResult bindingResult) {
        User userFromDB = userService.findByUsername(user.getUsername());

        if (userFromDB != null) {
            model.addAttribute("alreadyRegisteredMessage",
                    "Oops!  There is already a user registered with the email provided.");
            bindingResult.reject("email");
            return "register";
        }

        if (bindingResult.hasErrors()) {
            return "redirect:/register";
        } else {
            userService.saveUser(user);
            return "success";
        }
    }

    /**
     * REST API endpoint for user registration
     * POST /api/register
     * Cloud-ready: Stateless operation
     */
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerUserApi(@Valid @RequestBody User user, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation errors");
            response.put("errors", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }
        
        User userFromDB = userService.findByUsername(user.getUsername());
        if (userFromDB != null) {
            response.put("success", false);
            response.put("message", "User already exists with this username");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        userService.saveUser(user);
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("userId", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
