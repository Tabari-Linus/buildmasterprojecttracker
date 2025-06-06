package lii.buildmaster.projecttracker.service.impl;


import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.service.AuditLogService;
import lii.buildmaster.projecttracker.service.ProjectService;
import lii.buildmaster.projecttracker.util.AuditUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;
    private final AuditUtil auditUtil;

    public ProjectServiceImpl(ProjectRepository projectRepository, AuditLogService auditLogService,
                              AuditUtil auditUtil) {
        this.projectRepository = projectRepository;
        this.auditLogService = auditLogService;
        this.auditUtil = auditUtil;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public Project createProject(String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = new Project(name, description, deadline, status);
        Project savedProject = projectRepository.save(project);

        Map<String, Object> payload = auditUtil.createProjectAuditPayload(savedProject);
        auditLogService.logAction(
                ActionType.CREATE,
                EntityType.PROJECT,
                savedProject.getId().toString(),
                auditUtil.getCurrentActorName(),
                payload
        );

        return savedProject;
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
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'status_' + #status.name()")
    public List<Project> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", key = "'all'"),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public Project updateProject(Long id, String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        Map<String, Object> beforeState = auditUtil.createProjectAuditPayload(project);
        ProjectStatus oldStatus = project.getStatus();

        project.setName(name);
        project.setDescription(description);
        project.setDeadline(deadline);
        project.setStatus(status);

        Project updatedProject = projectRepository.save(project);

        Map<String, Object> afterState = auditUtil.createProjectAuditPayload(updatedProject);

        auditLogService.logAction(
                ActionType.UPDATE,
                EntityType.PROJECT,
                updatedProject.getId().toString(),
                auditUtil.getCurrentActorName(),
                beforeState,
                afterState
        );

        if (!oldStatus.equals(status)) {
            Map<String, Object> statusPayload = auditUtil.createStatusChangeAuditPayload(
                    updatedProject, "Project", oldStatus, status
            );
            auditLogService.logAction(
                    ActionType.STATUS_CHANGE,
                    EntityType.PROJECT,
                    updatedProject.getId().toString(),
                    auditUtil.getCurrentActorName(),
                    statusPayload
            );
        }
        return updatedProject;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        Map<String, Object> payload = auditUtil.createProjectAuditPayload(project);
        payload.put("deletedTasksCount", project.getTasks().size());

        projectRepository.deleteById(id);

        auditLogService.logAction(
                ActionType.DELETE,
                EntityType.PROJECT,
                id.toString(),
                auditUtil.getCurrentActorName(),
                payload
        );
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
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", key = "'all'"),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public Project markAsCompleted(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        ProjectStatus oldStatus = project.getStatus();
        project.setStatus(ProjectStatus.COMPLETED);
        Project savedProject = projectRepository.save(project);

        Map<String, Object> payload = auditUtil.createStatusChangeAuditPayload(
                savedProject, "Project", oldStatus, ProjectStatus.COMPLETED
        );
        auditLogService.logAction(
                ActionType.STATUS_CHANGE,
                EntityType.PROJECT,
                savedProject.getId().toString(),
                auditUtil.getCurrentActorName(),
                payload
        );

        return savedProject;
    }
}
