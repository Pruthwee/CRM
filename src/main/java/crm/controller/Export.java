import crm.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Cloud-Ready Export Controller with RESTful API support
 */

    private UserService userService;

    public Export(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handle request to download an Excel document
     */
    @GetMapping("/download")
    /**
     * REST API: Get all users for export
     * Cloud-ready endpoint for API gateways and load balancers
     */
    @GetMapping("/api/export/users")
    @ResponseBody
    public ResponseEntity<List<User>> exportUsersApi() {
        List<User> users = (List<User>) userService.listAllUsers();
        return ResponseEntity.ok(users);
    }

}
