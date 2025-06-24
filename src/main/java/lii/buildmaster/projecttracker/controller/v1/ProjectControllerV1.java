package lii.buildmaster.projecttracker.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lii.buildmaster.projecttracker.mapper.ProjectMapper;
import lii.buildmaster.projecttracker.model.dto.request.ProjectRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.ProjectSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.model.dto.response.ApiResponse;
import lii.buildmaster.projecttracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Operations for managing projects and performing analysis")
@RequiredArgsConstructor
public class ProjectControllerV1 {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get paginated list of projects")
    public ResponseEntity<ApiResponse<Page<ProjectSummaryDto>>> getAllProjects(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<Project> projectPage = projectService.getAllProjects(pageable);
        Page<ProjectSummaryDto> dtoPage = projectPage.map(projectMapper::toSummaryDto);

        return ResponseEntity.ok(new ApiResponse<>(true, dtoPage, "Projects retrieved successfully"));
    }


    @GetMapping("/{id}")
    @PreAuthorize("@security.canAccessProject(#id)")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProjectById(@PathVariable Long id) {
        ProjectResponseDto responseDto = projectService.getProjectById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, responseDto, "Project retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(@Valid @RequestBody ProjectRequestDto requestDto) {
        Project project = projectService.createProject(
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getDeadline(),
                requestDto.getStatus()
        );
        ProjectResponseDto responseDto = projectMapper.toResponseDto(project);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, responseDto, "Project created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@security.canModifyProject(#id)")
    @Operation(summary = "Update existing project")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDto requestDto) {

        Project updatedProject = projectService.updateProject(
                id,
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getDeadline(),
                requestDto.getStatus()
        );

        ProjectResponseDto responseDto = projectMapper.toResponseDto(updatedProject);
        return ResponseEntity.ok(new ApiResponse<>(true, responseDto, "Project updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a project by ID")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "Project deleted successfully"));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Filter projects by status")
    public ResponseEntity<ApiResponse<List<ProjectSummaryDto>>> getProjectsByStatus(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam ProjectStatus status) {

        Page<ProjectResponseDto> projects = projectService.getProjectsByStatus(status, pageable);
        List<ProjectSummaryDto> projectDtos = projects.map(projectMapper::toSummaryDto).getContent();

        return ResponseEntity.ok(new ApiResponse<>(true, projectDtos, "Projects filtered by status"));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search projects by name")
    public ResponseEntity<ApiResponse<List<ProjectSummaryDto>>> searchProjects(@RequestParam String name) {
        List<Project> projects = projectService.searchProjectsByName(name);
        List<ProjectSummaryDto> projectDtos = projects.stream()
                .map(projectMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, projectDtos, "Project search results"));
    }

    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get list of overdue projects")
    public ResponseEntity<ApiResponse<List<ProjectSummaryDto>>> getOverdueProjects() {
        List<Project> projects = projectService.getOverdueProjects();
        List<ProjectSummaryDto> projectDtos = projects.stream()
                .map(projectMapper::toSummaryDto)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, projectDtos, "Overdue projects fetched"));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Mark project as completed")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> markAsCompleted(@PathVariable Long id) {
        Project project = projectService.markAsCompleted(id);
        ProjectResponseDto responseDto = projectMapper.toResponseDto(project);
        return ResponseEntity.ok(new ApiResponse<>(true, responseDto, "Project marked as completed"));
    }

    @GetMapping("/stats/count-by-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get project count by status")
    public ResponseEntity<ApiResponse<java.util.Map<ProjectStatus, Long>>> getProjectCountsByStatus() {
        java.util.Map<ProjectStatus, Long> counts = new java.util.HashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            counts.put(status, projectService.getProjectCountByStatus(status));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, counts, "Project counts by status retrieved"));
    }
}
