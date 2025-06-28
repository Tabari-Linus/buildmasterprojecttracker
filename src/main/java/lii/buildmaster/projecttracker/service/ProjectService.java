package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.ProjectSummaryDto;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProjectService {

    ProjectResponseDto createProject(String name, String description, LocalDateTime deadline, ProjectStatus status);

    Page<ProjectSummaryDto> getAllProjects(Pageable pageable);

    ProjectResponseDto getProjectById(Long id);

    Page<ProjectSummaryDto> getProjectsByStatus(ProjectStatus status, Pageable pageable);

    ProjectResponseDto updateProject(Long id, String name, String description, LocalDateTime deadline, ProjectStatus status);

    void deleteProject(Long id);

    List<ProjectSummaryDto> getOverdueProjects();

    long getProjectCountByStatus(ProjectStatus status);

    List<ProjectSummaryDto> searchProjectsByName(String name);

    ProjectResponseDto markAsCompleted(Long id);

    Map<ProjectStatus, Long> getProjectCountsByStatus();
}
