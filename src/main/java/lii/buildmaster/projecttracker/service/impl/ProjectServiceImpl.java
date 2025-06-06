package lii.buildmaster.projecttracker.service.impl;


import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.exception.ProjectNotFoundException;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.service.ProjectService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Auditable(action = ActionType.CREATE, entityType = EntityType.PROJECT)
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public Project createProject(String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = new Project(name, description, deadline, status);
        return projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'all'")
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#id")
    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'status_' + #status.name()")
    public List<Project> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    @Override
    @Auditable(action = ActionType.UPDATE, entityType = EntityType.PROJECT)
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", key = "'all'"),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public Project updateProject(Long id, String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        project.setName(name);
        project.setDescription(description);
        project.setDeadline(deadline);
        project.setStatus(status);

        return projectRepository.save(project);
    }

    @Override
    @Auditable(action = ActionType.DELETE, entityType = EntityType.PROJECT)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        projectRepository.deleteById(id);

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'overdue'")
    public List<Project> getOverdueProjects() {
        return projectRepository.findOverdueProjects(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectStats", key = "'count_' + #status.name()")
    public long getProjectCountByStatus(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'search_' + #name")
    public List<Project> searchProjectsByName(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Auditable(action = ActionType.STATUS_CHANGE, entityType = EntityType.PROJECT)
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", key = "'all'"),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public Project markAsCompleted(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        project.setStatus(ProjectStatus.COMPLETED);
        return projectRepository.save(project);

    }
}
