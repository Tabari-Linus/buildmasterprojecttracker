package lii.buildmaster.projecttracker.controller.v1;

import lii.buildmaster.projecttracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rbac-test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RbacTestController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getCurrentUserInfo() {
        Map<String, Object> info = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        info.put("username", auth.getName());
        info.put("authorities", auth.getAuthorities());
        info.put("isAdmin", SecurityUtils.isAdmin());
        info.put("isManager", SecurityUtils.isManager());
        info.put("isDeveloper", SecurityUtils.isDeveloper());
        info.put("isContractor", SecurityUtils.isContractor());
        info.put("currentUserId", SecurityUtils.getCurrentUserId());

        return info;
    }

    /**
     * Admin only endpoint
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Map<String, String> adminOnly() {
        return Map.of("message", "This is an admin-only resource",
                "user", SecurityUtils.getCurrentUsername());
    }

    /**
     * Manager only endpoint
     */
    @GetMapping("/manager-only")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public Map<String, String> managerOnly() {
        return Map.of("message", "This is a manager-only resource",
                "user", SecurityUtils.getCurrentUsername());
    }

    /**
     * Developer only endpoint
     */
    @GetMapping("/developer-only")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public Map<String, String> developerOnly() {
        return Map.of("message", "This is a developer-only resource",
                "user", SecurityUtils.getCurrentUsername());
    }

    /**
     * Manager or Admin endpoint
     */
    @GetMapping("/manager-or-admin")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public Map<String, String> managerOrAdmin() {
        return Map.of("message", "This resource is for managers and admins",
                "user", SecurityUtils.getCurrentUsername());
    }

    /**
     * All authenticated users
     */
    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> authenticatedOnly() {
        return Map.of("message", "This resource is for all authenticated users",
                "user", SecurityUtils.getCurrentUsername());
    }

    /**
     * Test custom security expression
     */
    @GetMapping("/test-project-access/{projectId}")
    @PreAuthorize("@security.canAccessProject(#projectId)")
    public Map<String, Object> testProjectAccess(@PathVariable Long projectId) {
        return Map.of("message", "You have access to project " + projectId,
                "user", SecurityUtils.getCurrentUsername(),
                "projectId", projectId);
    }
}