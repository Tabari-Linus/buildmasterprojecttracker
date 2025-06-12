package lii.buildmaster.projecttracker.controller.v1;

import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/auth/init")
@RequiredArgsConstructor
public class InitController {

    private static final Logger logger = LoggerFactory.getLogger(InitController.class);

    private final RoleRepository roleRepository;

    @GetMapping("/roles")
    public Map<String, Object> initializeRoles() {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("Starting role initialization...");

            // First, let's check what roles exist
            long existingRoles = roleRepository.count();
            result.put("existingRolesCount", existingRoles);
            logger.info("Existing roles count: {}", existingRoles);

            // Initialize all roles
            Arrays.stream(RoleName.values()).forEach(roleName -> {
                try {
                    if (!roleRepository.existsByName(roleName)) {
                        Role role = Role.builder()
                                .name(roleName)
                                .description(roleName.getDescription())
                                .build();
                        roleRepository.save(role);
                        result.put("created_" + roleName, true);
                        logger.info("Created role: {}", roleName);
                    } else {
                        result.put("exists_" + roleName, true);
                        logger.info("Role {} already exists", roleName);
                    }
                } catch (Exception e) {
                    logger.error("Error creating role {}: {}", roleName, e.getMessage(), e);
                    result.put("error_" + roleName, e.getMessage());
                }
            });

            result.put("status", "success");
            result.put("totalRoles", roleRepository.count());
            logger.info("Role initialization completed. Total roles: {}", roleRepository.count());

        } catch (Exception e) {
            logger.error("Error during role initialization", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("type", e.getClass().getSimpleName());
            result.put("cause", e.getCause() != null ? e.getCause().getMessage() : "No cause");
        }

        return result;
    }

    @GetMapping("/check")
    public Map<String, Object> checkSystem() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Check if repository is working
            result.put("repositoryWorking", true);
            result.put("roleCount", roleRepository.count());

            // List all role names
            result.put("availableRoleNames", Arrays.stream(RoleName.values())
                    .map(Enum::name)
                    .toArray());

            // Check if we can query
            for (RoleName roleName : RoleName.values()) {
                result.put("exists_" + roleName, roleRepository.existsByName(roleName));
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }

        return result;
    }
}