package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.exception.UserNotFoundException;
import lii.buildmaster.projecttracker.mapper.UserMapper;
import lii.buildmaster.projecttracker.model.dto.request.UserRoleUpdateRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.UserResponseDto;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled(), user.getRoles()));

    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled(), user.getRoles()))
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public UserResponseDto updateUserRoles(Long id, UserRoleUpdateRequestDto roleUpdateRequest) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setRoles(roleUpdateRequest.getRoles());
                    userRepository.save(user);
                    return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled(), user.getRoles());
                })
                .orElseThrow(() -> new UserNotFoundException(id)
                );

    }

    @Override
    public UserResponseDto updateUserStatus(Long id, boolean enabled) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(enabled);
                    userRepository.save(user);
                    return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled(), user.getRoles());
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public Page<UserResponseDto> getUsersByRole(RoleName roleName, Pageable pageable) {
        Page<User> users = userRepository.findByRolesName(roleName, pageable);
        return users.map(userMapper::toResponseDto);

    }

    @Override
    public Map<String, Object> getSystemStatistics() {
        return Map.of();
    }

    @Override
    public Page<?> getAuditLogs(String entityType, String actionType, Pageable pageable) {
        return null;
    }

    @Override
    public UserResponseDto approveContractor(Long id, String newRole) {
        return null;
    }

    @Override
    public String resetUserPassword(Long id) {
        return "";
    }
}
