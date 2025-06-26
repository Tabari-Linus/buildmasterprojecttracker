package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.exception.UserNotFoundException;
import lii.buildmaster.projecttracker.mapper.UserMapper;
import lii.buildmaster.projecttracker.model.dto.request.UserRoleUpdateRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.UserResponseDto;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.service.AdminService;
import lii.buildmaster.projecttracker.util.AuthenticationUtil;
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
    private final ProjectRepository projectRepository;

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
        long totalUsers = userRepository.count();
        long enabledUsers = userRepository.countByEnabled(true);
        long disabledUsers = userRepository.countByEnabled(false);
        long contractors = userRepository.findByRoleName(RoleName.ROLE_CONTRACTOR).size();
        long admins = userRepository.findByRoleName(RoleName.ROLE_ADMIN).size();
        long developers = userRepository.findByRoleName(RoleName.ROLE_DEVELOPER).size();
        long managers = userRepository.findByRoleName(RoleName.ROLE_MANAGER).size();
        long numberOfProjects = projectRepository.count();
        long numberOfTasks = projectRepository.findAll().stream()
                .mapToLong(project -> project.getTasks().size())
                .sum();

        return Map.of(
                "totalUsers", totalUsers,
                "enabledUsers", enabledUsers,
                "disabledUsers", disabledUsers,
                "contractors", contractors,
                "admins", admins,
                "developers", developers,
                "managers", managers,
                "numberOfProjects", numberOfProjects,
                "numberOfTasks", numberOfTasks
        );
    }


    @Override
    public UserResponseDto approveContractor(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (!user.getRoles().contains(RoleName.ROLE_CONTRACTOR)) {
            throw new IllegalArgumentException("User is not a contractor");
        }
        user.setEnabled(true);
        return userMapper.toResponseDto(user);
    }

    @Override
    public String resetUserPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        String tempPassword = AuthenticationUtil.generateTemporaryPassword();
        user.setPassword(tempPassword);
        userRepository.save(user);
        return tempPassword;
    }
}
