package lii.buildmaster.projecttracker.controller.v1;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content - No authentication required.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('MANAGER') or hasRole('ADMIN') or hasRole('CONTRACTOR')")
    public String userAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "User Content - Hello " + auth.getName() + "!";
    }

    @GetMapping("/dev")
    @PreAuthorize("hasRole('DEVELOPER')")
    public String developerAccess() {
        return "Developer Board - Only developers can see this.";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public String managerAccess() {
        return "Manager Board - Only managers can see this.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board - Only admins can see this.";
    }

    @GetMapping("/contractor")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public String contractorAccess() {
        return "Contractor Board - Only contractors can see this.";
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return String.format("Current user: %s, Authorities: %s",
                auth.getName(),
                auth.getAuthorities());
    }
}
