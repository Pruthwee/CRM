package crm.controller;

import crm.entity.User;
import crm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Cloud-ready Export Controller
 * - REST API endpoints for cloud load balancers
 * - Stateless operations
 */
@Controller
public class Export {

    private UserService userService;

    public Export(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handle request to download an Excel document
     */
    @GetMapping("/download")
    public String download(Model model) {
        model.addAttribute("users", userService.listAllUsers());
        return "";
    }

    /**
     * REST API endpoint to get users for export
     * GET /api/export/users
     * Cloud-ready: Stateless operation
     */
    @GetMapping("/api/export/users")
    @ResponseBody
    public ResponseEntity<List<User>> exportUsersApi() {
        List<User> users = (List<User>) userService.listAllUsers();
        return ResponseEntity.ok(users);
    }

}
