package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectService {

    Project createProject(String name, String description, LocalDateTime deadline, ProjectStatus status);

    List<Project> getAllProjects();

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'all'")
    Page<ProjectResponseDto> getAllProjects(Pageable pageable);

    ProjectResponseDto getProjectById(Long id);

    List<Project> getProjectsByStatus(ProjectStatus status);

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'status_' + #status.name()")
    Page<ProjectResponseDto> getProjectsByStatus(String status, Pageable pageable);

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "'status_' + #status.name()")
    Page<ProjectResponseDto> getProjectsByStatus(ProjectStatus status, Pageable pageable);

    Project updateProject(Long id, String name, String description, LocalDateTime deadline, ProjectStatus status);

    void deleteProject(Long id);

    List<Project> getOverdueProjects();

    long getProjectCountByStatus(ProjectStatus status);

    List<Project> searchProjectsByName(String name);

    Project markAsCompleted(Long id);
}
