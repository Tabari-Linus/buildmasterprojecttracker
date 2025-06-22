package lii.buildmaster.projecttracker.config;

import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.TaskRequestDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.service.DeveloperService;
import lii.buildmaster.projecttracker.service.ProjectService;
import lii.buildmaster.projecttracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DataInitializationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;

    private final ProjectService projectService;
    private final DeveloperService developerService;
    private final TaskService taskService;

    public void initialize() {
        createRoles();
        var project1 = createProject("E-Commerce Platform", "Building a modern e-commerce platform with microservices architecture", 6, ProjectStatus.IN_PROGRESS);
        var project2 = createProject("Mobile Banking App", "Secure mobile banking application with biometric authentication", 4, ProjectStatus.PLANNING);
        var overdueProject = createProject("Legacy System Migration", "Migrating legacy systems to cloud infrastructure", -1, ProjectStatus.ON_HOLD);

        var dev1 = createDeveloper(
                DeveloperRequestDto.builder()
                        .name("Kwame Oduru")
                        .email("kwame.oduru@buildmaster.com")
                        .skills("Java, Spring Boot, React, PostgreSQL, Docker")
                        .build()
        );
        var dev2 = createDeveloper(
                DeveloperRequestDto.builder()
                        .name("David Mawuli")
                        .email("david.mawuli@buildmaster.com")
                        .skills("Java, Spring Boot, Microservices, MySQL, Jenkins")
                        .build()
        );

        createTask("User Authentication Module", "Implement user authentication with JWT and OAuth2", TaskStatus.TODO, 10, project1.getId(), dev1.getId());
        createTask("Payment Gateway Integration", "Integrate payment gateway for secure transactions", TaskStatus.IN_PROGRESS, 15, project1.getId(), null);
        createTask("UI/UX Design", "Design user-friendly interface for the e-commerce platform", TaskStatus.DONE, 5, project1.getId(), dev2.getId());

        createAdminUser();
        createSampleUser("manager1", "manager@projecttracker.com", "Manager", "One", RoleName.ROLE_MANAGER);
        createSampleUser("developercoder", "developercoder@gmail.com", "Developer", "Coder", RoleName.ROLE_DEVELOPER);
    }

    private void createRoles() {
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(roleName.getDescription())
                        .build());
            }
        });
    }

    private lii.buildmaster.projecttracker.model.entity.Project createProject(String name, String description, int monthsToAdd, ProjectStatus status) {
        return projectService.createProject(
                name,
                description,
                LocalDateTime.now().plusMonths(monthsToAdd),
                status
        );
    }

    private Developer createDeveloper(DeveloperRequestDto dto) {
        return developerService.createDeveloper(dto);
    }

    private void createTask(String title, String description, TaskStatus status, int dueInDays, Long projectId, Long developerId) {
        TaskRequestDto.TaskRequestDtoBuilder builder = TaskRequestDto.builder()
                .title(title)
                .description(description)
                .status(status)
                .dueDate(LocalDateTime.now().plusDays(dueInDays))
                .projectId(projectId);

        if (developerId != null) {
            builder.developerId(developerId);
        }

        taskService.createTask(builder.build());
    }

    private void createAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@projecttracker.com")
                    .password(passwordEncoder.encode("admin12345!"))
                    .firstName("System")
                    .lastName("Administrator")
                    .provider(AuthProvider.LOCAL)
                    .roles(Set.of(adminRole))
                    .enabled(true)
                    .build();

            userRepository.save(admin);
        }
    }

    private void createSampleUser(String username, String email, String firstName, String lastName, RoleName roleName) {
        if (!userRepository.existsByUsername(username)) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException(roleName + " role not found"));

            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode("password12345!"))
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