package lii.buildmaster.projecttracker.controller;

import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }


    @PostMapping("/test")
    public Project createTestProject() {
        Project project = new Project(
                "Test Project",
                "This is a test project created via API",
                LocalDateTime.now().plusDays(30),
                ProjectStatus.PLANNING
        );
        return projectRepository.save(project);
    }


    @GetMapping("/status/{status}")
    public List<Project> getProjectsByStatus(@PathVariable ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }


    @GetMapping("/count")
    public long getProjectCount() {
        return projectRepository.count();
    }
}
