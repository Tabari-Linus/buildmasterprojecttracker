package lii.buildmaster.projecttracker.config;

import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional  // Important!
public class DataInitializationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;

    public void initialize() {
        // Create roles
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName.getDescription())
                        .build();
                roleRepository.save(role);
            }
        });

        // Create admin
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@projecttracker.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(adminRole))
                    .enabled(true)
                    .build();

            userRepository.save(admin);
        }

        // Create sample users
        createSampleUser("manager1", "manager@projecttracker.com", "Manager", "One", RoleName.ROLE_MANAGER);
        createSampleUser("dev1", "dev1@projecttracker.com", "Developer", "One", RoleName.ROLE_DEVELOPER);
        createSampleUser("dev2", "dev2@projecttracker.com", "Developer", "Two", RoleName.ROLE_DEVELOPER);
    }

    private void createSampleUser(String username, String email, String firstName, String lastName, RoleName roleName) {
        if (!userRepository.existsByUsername(username)) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException(roleName + " role not found"));

            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode("password123"))
                    .firstName(firstName)
                    .lastName(lastName)
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(role))
                    .enabled(true)
                    .build();

            User savedUser = userRepository.save(user);

            if (roleName == RoleName.ROLE_DEVELOPER) {
                Developer dev = new Developer();
                dev.setName(firstName + " " + lastName);
                dev.setEmail(email);
                dev.setUser(savedUser);
                dev.setSkills("Java, Spring Boot");
                developerRepository.save(dev);
            }
        }
    }
}
