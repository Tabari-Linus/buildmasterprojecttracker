//package lii.buildmaster.projecttracker.config;
//
//import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
//import lii.buildmaster.projecttracker.model.enums.TaskStatus;
//import lii.buildmaster.projecttracker.service.DeveloperService;
//import lii.buildmaster.projecttracker.service.ProjectService;
//import lii.buildmaster.projecttracker.service.TaskService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//@Order(1)
//public class DataInitializer implements CommandLineRunner {
//
//    private final ProjectService projectService;
//    private final DeveloperService developerService;
//    private final TaskService taskService;
//
////    private static boolean initialized = false;
//
//    public DataInitializer(ProjectService projectService,
//                           DeveloperService developerService,
//                           TaskService taskService) {
//        this.projectService = projectService;
//        this.developerService = developerService;
//        this.taskService = taskService;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        var project1 = projectService.createProject(
//                "E-Commerce Platform",
//                "Building a modern e-commerce platform with microservices architecture",
//                LocalDateTime.now().plusMonths(6),
//                ProjectStatus.IN_PROGRESS
//        );
//
//        var project2 = projectService.createProject(
//                "Mobile Banking App",
//                "Secure mobile banking application with biometric authentication",
//                LocalDateTime.now().plusMonths(4),
//                ProjectStatus.PLANNING
//        );
//
//        var overdueProject = projectService.createProject(
//                "Legacy System Migration",
//                "Migrating legacy systems to cloud infrastructure",
//                LocalDateTime.now().minusDays(30), // Past deadline
//                ProjectStatus.ON_HOLD
//        );
//
//        var dev1 = developerService.createDeveloper(
//                "Kwame Oduru",
//                "kwame.oduru@buildmaster.com",
//                "Java, Spring Boot, React, PostgreSQL, Docker"
//        );
//
//        var dev4 = developerService.createDeveloper(
//                "David Mawuli",
//                "david.mawuli@buildmaster.com",
//                "Java, Spring Boot, Microservices, MySQL, Jenkins"
//        );
//
//        taskService.createTask(
//                "Setup Project Structure",
//                "Initialize the project with proper package structure and dependencies",
//                TaskStatus.DONE,
//                LocalDateTime.now().plusDays(2),
//                project1.getId(),
//                dev1.getId()
//        );
//
//        taskService.createTask(
//                "Implement User Authentication",
//                "Create JWT-based authentication system with role management",
//                TaskStatus.IN_PROGRESS,
//                LocalDateTime.now().plusDays(10),
//                project1.getId(),
//                dev1.getId()
//        );
//
//        taskService.createTask(
//                "Shopping Cart Implementation",
//                "Implement shopping cart functionality with session management",
//                TaskStatus.TODO,
//                LocalDateTime.now().plusDays(20),
//                project1.getId()
//        );
//    }
//
//
//}