package lii.buildmaster.projecttracker.service;


import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Project createProject(String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = new Project(name, description, deadline, status);
        return projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    @Override
    public Project updateProject(Long id, String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        project.setName(name);
        project.setDescription(description);
        project.setDeadline(deadline);
        project.setStatus(status);

        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getOverdueProjects() {
        return projectRepository.findOverdueProjects(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long getProjectCountByStatus(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> searchProjectsByName(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Project markAsCompleted(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        project.setStatus(ProjectStatus.COMPLETED);
        return projectRepository.save(project);
    }
}
