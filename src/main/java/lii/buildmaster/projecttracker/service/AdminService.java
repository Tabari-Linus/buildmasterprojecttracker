package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.dto.request.UserRoleUpdateRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.UserResponseDto;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public interface AdminService {

    Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserById(@PathVariable Long id);

    UserResponseDto updateUserRoles(@PathVariable Long id, UserRoleUpdateRequestDto roleUpdateRequest);

    UserResponseDto updateUserStatus(@PathVariable Long id, boolean enabled);

    void deleteUser(@PathVariable Long id);

    Page<UserResponseDto> getUsersByRole(@PathVariable RoleName roleName, Pageable pageable);

    Map<String, Object> getSystemStatistics();

    Page<? > getAuditLogs(String entityType, String actionType, Pageable pageable);

    UserResponseDto approveContractor(
            @PathVariable Long id,
            @RequestParam String newRole);

    String resetUserPassword(@PathVariable Long id);

}
