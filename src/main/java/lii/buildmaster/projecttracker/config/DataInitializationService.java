package lii.buildmaster.projecttracker.config;

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
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName.getDescription())
                        .build();
                roleRepository.save(role);
            }
        });

        var project1 = projectService.createProject(
                "E-Commerce Platform",
                "Building a modern e-commerce platform with microservices architecture",
                LocalDateTime.now().plusMonths(6),
                ProjectStatus.IN_PROGRESS
        );

        var project2 = projectService.createProject(
                "Mobile Banking App",
                "Secure mobile banking application with biometric authentication",
                LocalDateTime.now().plusMonths(4),
                ProjectStatus.PLANNING
        );

        var overdueProject = projectService.createProject(
                "Legacy System Migration",
                "Migrating legacy systems to cloud infrastructure",
                LocalDateTime.now().minusDays(30), // Past deadline
                ProjectStatus.ON_HOLD
        );

        var dev1 = developerService.createDeveloper(
                "Kwame Oduru",
                "kwame.oduru@buildmaster.com",
                "Java, Spring Boot, React, PostgreSQL, Docker"
        );

        var dev4 = developerService.createDeveloper(
                "David Mawuli",
                "david.mawuli@buildmaster.com",
                "Java, Spring Boot, Microservices, MySQL, Jenkins"
        );

        TaskRequestDto taskRequestDto = TaskRequestDto.builder()
                .title("User Authentication Module")
                .description("Implement user authentication with JWT and OAuth2")
                .status(TaskStatus.TODO)
                .dueDate(LocalDateTime.now().plusDays(10))
                .projectId(project1.getId())
                .developerId(dev1.getId())
                .build();
        taskService.createTask(taskRequestDto);

        taskRequestDto = TaskRequestDto.builder()
                .title("Payment Gateway Integration")
                .description("Integrate payment gateway for secure transactions")
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(LocalDateTime.now().plusDays(15))
                .projectId(project1.getId())
                .build();
        taskService.createTask(taskRequestDto);

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

        createSampleUser("manager1", "manager@projecttracker.com", "Manager", "One", RoleName.ROLE_MANAGER);
        createSampleUser("developercoder", "developercoder@gmail.com", "Developer", "coder", RoleName.ROLE_DEVELOPER);
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
