package lii.buildmaster.projecttracker.config;

import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lii.buildmaster.projecttracker.service.DeveloperService;
import lii.buildmaster.projecttracker.service.ProjectService;
import lii.buildmaster.projecttracker.service.TaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final ProjectService projectService;
    private final DeveloperService developerService;
    private final TaskService taskService;

//    private static boolean initialized = false;

    public DataInitializer(ProjectService projectService,
                           DeveloperService developerService,
                           TaskService taskService) {
        this.projectService = projectService;
        this.developerService = developerService;
        this.taskService = taskService;
    }

    @Override
    public void run(String... args) throws Exception {
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

        taskService.createTask(
                "Setup Project Structure",
                "Initialize the project with proper package structure and dependencies",
                TaskStatus.DONE,
                LocalDateTime.now().plusDays(2),
                project1.getId(),
                dev1.getId()
        );

        taskService.createTask(
                "Implement User Authentication",
                "Create JWT-based authentication system with role management",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(10),
                project1.getId(),
                dev1.getId()
        );

        taskService.createTask(
                "Shopping Cart Implementation",
                "Implement shopping cart functionality with session management",
                TaskStatus.TODO,
                LocalDateTime.now().plusDays(20),
                project1.getId()
        );
    }

//    private void initializeSampleData() {
//        // Create Projects
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
//        var project3 = projectService.createProject(
//                "Data Analytics Dashboard",
//                "Real-time analytics dashboard for business intelligence",
//                LocalDateTime.now().plusMonths(3),
//                ProjectStatus.IN_PROGRESS
//        );
//
//        // Create an overdue project for testing
//        var overdueProject = projectService.createProject(
//                "Legacy System Migration",
//                "Migrating legacy systems to cloud infrastructure",
//                LocalDateTime.now().minusDays(30), // Past deadline
//                ProjectStatus.ON_HOLD
//        );
//
//        // Create Developers
//        var dev1 = developerService.createDeveloper(
//                "Alice Johnson",
//                "alice.johnson@buildmaster.com",
//                "Java, Spring Boot, React, PostgreSQL, Docker"
//        );
//
//        var dev2 = developerService.createDeveloper(
//                "Bob Smith",
//                "bob.smith@buildmaster.com",
//                "JavaScript, Node.js, MongoDB, AWS, Kubernetes"
//        );
//
//        var dev3 = developerService.createDeveloper(
//                "Carol Williams",
//                "carol.williams@buildmaster.com",
//                "Python, Django, Machine Learning, PostgreSQL, Redis"
//        );
//
//        var dev4 = developerService.createDeveloper(
//                "David Brown",
//                "david.brown@buildmaster.com",
//                "Java, Spring Boot, Microservices, MySQL, Jenkins"
//        );
//
//        // Create Tasks for E-Commerce Platform
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
//                "Design Product Catalog API",
//                "RESTful API for product management with search and filtering",
//                TaskStatus.TODO,
//                LocalDateTime.now().plusDays(15),
//                project1.getId(),
//                dev2.getId()
//        );
//
//        taskService.createTask(
//                "Shopping Cart Implementation",
//                "Implement shopping cart functionality with session management",
//                TaskStatus.TODO,
//                LocalDateTime.now().plusDays(20),
//                project1.getId()
//        ); // Unassigned task
//
//        // Create Tasks for Mobile Banking App
//        taskService.createTask(
//                "Security Requirements Analysis",
//                "Analyze security requirements and compliance standards",
//                TaskStatus.IN_PROGRESS,
//                LocalDateTime.now().plusDays(7),
//                project2.getId(),
//                dev4.getId()
//        );
//
//        taskService.createTask(
//                "Biometric Authentication Setup",
//                "Implement fingerprint and face recognition authentication",
//                TaskStatus.TODO,
//                LocalDateTime.now().plusDays(25),
//                project2.getId(),
//                dev3.getId()
//        );
//
//        // Create Tasks for Data Analytics Dashboard
//        taskService.createTask(
//                "Data Pipeline Architecture",
//                "Design and implement real-time data processing pipeline",
//                TaskStatus.IN_PROGRESS,
//                LocalDateTime.now().plusDays(12),
//                project3.getId(),
//                dev3.getId()
//        );
//
//        taskService.createTask(
//                "Dashboard UI Components",
//                "Create reusable React components for data visualization",
//                TaskStatus.TODO,
//                LocalDateTime.now().plusDays(18),
//                project3.getId(),
//                dev2.getId()
//        );
//
//        // Create overdue tasks for testing
//        taskService.createTask(
//                "Database Migration Script",
//                "Create scripts to migrate data from legacy system",
//                TaskStatus.IN_PROGRESS,
//                LocalDateTime.now().minusDays(5), // Overdue
//                overdueProject.getId(),
//                dev4.getId()
//        );
//
//        taskService.createTask(
//                "Performance Testing",
//                "Conduct load testing on migrated systems",
//                TaskStatus.TODO,
//                LocalDateTime.now().minusDays(2), // Overdue
//                overdueProject.getId()
//        ); // Unassigned overdue task
//
//        System.out.println("📊 Sample data created:");
//    }

}