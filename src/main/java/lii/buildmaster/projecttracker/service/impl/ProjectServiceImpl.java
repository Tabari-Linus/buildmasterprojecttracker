package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.exception.ProjectNotFoundException;
import lii.buildmaster.projecttracker.mapper.ProjectMapper;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.repository.jpa.TaskRepository;
import lii.buildmaster.projecttracker.service.ProjectService;
import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.ProjectSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Auditable(action = ActionType.CREATE, entityType = EntityType.PROJECT)
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public ProjectResponseDto createProject(String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = new Project(name, description, deadline, status);
        Project savedProject = projectRepository.save(project);
        return mapProjectToResponseDtoWithCalculatedFields(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'all'", unless = "#result.isEmpty()")
    public Page<ProjectSummaryDto> getAllProjects(Pageable pageable) {
        Page<Project> projectPage = projectRepository.findAll(pageable);
        List<ProjectSummaryDto> dtoList = projectPage.getContent().stream()
                .map(this::mapProjectToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, projectPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#id")
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return mapProjectToResponseDtoWithCalculatedFields(project);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'status_' + #status.name()", unless = "#result.isEmpty()")
    public Page<ProjectSummaryDto> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        Page<Project> projectPage = projectRepository.findByStatus(status, pageable);
        List<ProjectSummaryDto> dtoList = projectPage.getContent().stream()
                .map(this::mapProjectToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, projectPage.getTotalElements());
    }

    @Override
    @Auditable(action = ActionType.UPDATE, entityType = EntityType.PROJECT)
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public ProjectResponseDto updateProject(Long id, String name, String description, LocalDateTime deadline, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        project.setName(name);
        project.setDescription(description);
        project.setDeadline(deadline);
        project.setStatus(status);

        Project updatedProject = projectRepository.save(project);
        return mapProjectToResponseDtoWithCalculatedFields(updatedProject);
    }

    @Override
    @Auditable(action = ActionType.DELETE, entityType = EntityType.PROJECT)
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));






        projectRepository.delete(project);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'overdue'", unless = "#result.isEmpty()")
    public List<ProjectSummaryDto> getOverdueProjects() {
        List<Project> projects = projectRepository.findOverdueProjects(LocalDateTime.now());
        return projects.stream()
                .map(this::mapProjectToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectStats", key = "'count_status_' + #status.name()")
    public long getProjectCountByStatus(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'search_name_' + #name", unless = "#result.isEmpty()")
    public List<ProjectSummaryDto> searchProjectsByName(String name) {
        List<Project> projects = projectRepository.findByNameContainingIgnoreCase(name);
        return projects.stream()
                .map(this::mapProjectToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
    }

    @Override
    @Auditable(action = ActionType.STATUS_CHANGE, entityType = EntityType.PROJECT)
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectStats", allEntries = true)
    })
    public ProjectResponseDto markAsCompleted(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        project.setStatus(ProjectStatus.COMPLETED);
        Project updatedProject = projectRepository.save(project);
        return mapProjectToResponseDtoWithCalculatedFields(updatedProject);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectStats", key = "'counts_by_status'")
    public Map<ProjectStatus, Long> getProjectCountsByStatus() {
        List<Object[]> results = projectRepository.getProjectCountsByStatus();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (ProjectStatus) result[0],
                        result -> (Long) result[1]
                ));
    }


    private ProjectResponseDto mapProjectToResponseDtoWithCalculatedFields(Project project) {
        ProjectResponseDto dto = projectMapper.toResponseDto(project);
        dto.setTaskCount(taskRepository.countByProjectId(project.getId()));
        dto.setCompletedTaskCount(taskRepository.countByProjectIdAndStatus(project.getId(), lii.buildmaster.projecttracker.model.enums.TaskStatus.DONE));
        return dto;
    }


    private ProjectSummaryDto mapProjectToSummaryDtoWithCalculatedFields(Project project) {
        ProjectSummaryDto dto = projectMapper.toSummaryDto(project);
        dto.setTaskCount(taskRepository.countByProjectId(project.getId()));
        dto.setCompletedTaskCount(taskRepository.countByProjectIdAndStatus(project.getId(), lii.buildmaster.projecttracker.model.enums.TaskStatus.DONE));
        return dto;
    }
}