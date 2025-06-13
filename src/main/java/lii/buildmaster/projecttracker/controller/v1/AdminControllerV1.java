package lii.buildmaster.projecttracker.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')") // All endpoints require ADMIN role
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<UserResponseDto> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest roleUpdateRequest) {
        return ResponseEntity.ok(adminService.updateUserRoles(id, roleUpdateRequest));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, enabled));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/role/{roleName}")
    public ResponseEntity<Page<UserResponseDto>> getUsersByRole(
            @PathVariable String roleName,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getUsersByRole(roleName, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStatistics());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<?>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getAuditLogs(entityType, action, pageable));
    }


    @PostMapping("/users/{id}/approve-contractor")
    public ResponseEntity<UserResponseDto> approveContractor(
            @PathVariable Long id,
            @RequestParam String newRole) {
        return ResponseEntity.ok(adminService.approveContractor(id, newRole));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetUserPassword(@PathVariable Long id) {
        String tempPassword = adminService.resetUserPassword(id);
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successful",
                "tempPassword", tempPassword
        ));
    }
}
