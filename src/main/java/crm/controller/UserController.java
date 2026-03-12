package crm.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

// Cloud-ready controller with REST API support
// Stateless operations compatible with horizontal scaling
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    /**
     * REST API endpoint to get all users as JSON
     * GET /user/api/list
     * Cloud-ready: Works with API gateways and load balancers
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<User>> getUsersApi() {
        List<User> users = (List<User>) userService.listAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * REST API endpoint to get single user as JSON
     * GET /user/api/{id}
     * Cloud-ready: Stateless operation
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<User> getUserApi(@PathVariable Long id) {
        User user = userService.showUser(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
     * /user/list
     * <p>
     * Shows all users
     *
     * @param model model to attributes to
     * @return user/list
     */
    @GetMapping("/list")
    public String showAllUsers(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        model.addAttribute("currentUser", userService.findByUsername(currentUser.getUsername()));
        model.addAttribute("users", userService.listAllUsers());
        return "user/list";
    }

    /**
     * /user/edit/{id}
     * <p>
     * Shows edit user form
     *
     * @param model model to attributes to
     * @param id    variable type long user id
     * @return user/edit
     */
    @GetMapping("/edit/{id}")
    public String showFormEditUser(Model model, @PathVariable Long id) {
        model.addAttribute("user", userService.showUser(id));
        return "user/edit";
    }

    /**
     * /user/edit/{id}
     * <p>
     * Processes edit user request
     *
     * @param id            variable type long user id
     * @param user          variable type User
     * @param bindingResult variable type BindingResult
     * @return redirect:/user/list
     */
    @PostMapping("/edit/{id}")
    public String processRequestEditUser(@PathVariable Long id, @Valid User user,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/user/edit/" + id;
        } else {
            userService.editUser(user);
            return "redirect:/user/list";
        }
    }

    /**
     * /user/delete/{id}
     * <p>
     * Deletes user
     *
     * @param id variable type long user id
     * @return redirect:/user/list
     */
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(userService.showUser(id));
        return "redirect:/user/list";
    }

}
