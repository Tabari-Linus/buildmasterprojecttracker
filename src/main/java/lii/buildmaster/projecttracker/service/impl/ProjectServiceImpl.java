package lii.buildmaster.projecttracker.service.impl;


import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.exception.ProjectNotFoundException;
import lii.buildmaster.projecttracker.exception.UnauthorizedException;
import lii.buildmaster.projecttracker.mapper.ProjectMapper;
import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.service.ProjectService;
import lii.buildmaster.projecttracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

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
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }


    @Override
    public List<Project> getAllProjects(Pageable pageable) {
        if (SecurityUtils.isAdmin() || SecurityUtils.isManager()) {
            return projectRepository.findAll();
        } else if (SecurityUtils.isDeveloper()) {
            String username = SecurityUtils.getCurrentUsername();
            return (List<Project>) projectRepository.findProjectsByDeveloperUsername(username, pageable );
        } else if (SecurityUtils.isContractor()) {
            // Contractors see limited project information
            return projectRepository.findAll(); // Note: You might want to return a simplified list for contractors
        } else {
            throw new UnauthorizedException("You don't have permission to view projects");
        }
    }



    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#id")
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        // Additional security check already done by @PreAuthorize
        // For contractors, you might want to return limited information
        if (SecurityUtils.isContractor()) {
            return projectMapper.toSummaryResponseDto(project);
        }

        return projectMapper.toResponseDto(project);
    }


    @Override
    public List<Project> getProjectsByStatus(String status) {
        ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
        return projectRepository.findProjectByStatus((projectStatus));
    }

    @Override
    public Page<ProjectResponseDto> getProjectsByStatus(String status, Pageable pageable) {
        ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
        Page<Project> projectPage = projectRepository.findByStatus(projectStatus, pageable);
        return projectPage.map(projectMapper::toResponseDto);

    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'status_' + #status.name()")
    @Override
    public Page<ProjectResponseDto> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        Page<Project> projects;

        if (SecurityUtils.isAdmin() || SecurityUtils.isManager()) {
            projects = projectRepository.findByStatus(status, pageable);
        } else if (SecurityUtils.isDeveloper()) {
            String username = SecurityUtils.getCurrentUsername();
            projects = projectRepository.findByStatusAndDeveloperUsername(status, username, pageable);
        } else if (SecurityUtils.isContractor()) {
            // Contractors see limited project information
            projects = (Page<Project>) projectRepository.findByStatus(status, pageable);
            // Note: You might want to return a simplified DTO for contractors
        } else {
            throw new UnauthorizedException("You don't have permission to view projects");
        }

        return projects.map(projectMapper::toResponseDto);
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
