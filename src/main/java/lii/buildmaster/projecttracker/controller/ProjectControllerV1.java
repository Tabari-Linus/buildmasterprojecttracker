package lii.buildmaster.projecttracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lii.buildmaster.projecttracker.mapper.ProjectMapper;
import lii.buildmaster.projecttracker.model.dto.request.ProjectRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.ProjectSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import jakarta.validation.Valid;
import lii.buildmaster.projecttracker.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Operations for managing projects and performing analysis")
public class ProjectControllerV1 {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectControllerV1(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @GetMapping
    public ResponseEntity<Page<ProjectSummaryDto>> getAllProjects(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        List<Project> projects = projectService.getAllProjects();
        List<ProjectSummaryDto> projectDtos = projects.stream()
                .map(projectMapper::toSummaryDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), projectDtos.size());
        List<ProjectSummaryDto> pageContent = projectDtos.subList(start, end);

        Page<ProjectSummaryDto> page = new PageImpl<>(pageContent, pageable, projectDtos.size());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id) {
        Optional<Project> project = projectService.getProjectById(id);

        if (project.isPresent()) {
            ProjectResponseDto responseDto = projectMapper.toResponseDto(project.get());
            return ResponseEntity.ok(responseDto);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto requestDto) {
        Project project = projectService.createProject(
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getDeadline(),
                requestDto.getStatus()
        );

        ProjectResponseDto responseDto = projectMapper.toResponseDto(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDto requestDto) {

        try {
            Project updatedProject = projectService.updateProject(
                    id,
                    requestDto.getName(),
                    requestDto.getDescription(),
                    requestDto.getDeadline(),
                    requestDto.getStatus()
            );

            ProjectResponseDto responseDto = projectMapper.toResponseDto(updatedProject);
            return ResponseEntity.ok(responseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectSummaryDto>> getProjectsByStatus(@PathVariable ProjectStatus status) {
        List<Project> projects = projectService.getProjectsByStatus(status);
        List<ProjectSummaryDto> projectDtos = projects.stream()
                .map(projectMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectSummaryDto>> searchProjects(@RequestParam String name) {
        List<Project> projects = projectService.searchProjectsByName(name);
        List<ProjectSummaryDto> projectDtos = projects.stream()
                .map(projectMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDtos);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ProjectSummaryDto>> getOverdueProjects() {
        List<Project> projects = projectService.getOverdueProjects();
        List<ProjectSummaryDto> projectDtos = projects.stream()
                .map(projectMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDtos);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ProjectResponseDto> markAsCompleted(@PathVariable Long id) {
        try {
            Project project = projectService.markAsCompleted(id);
            ProjectResponseDto responseDto = projectMapper.toResponseDto(project);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats/count-by-status")
    public ResponseEntity<java.util.Map<ProjectStatus, Long>> getProjectCountsByStatus() {
        java.util.Map<ProjectStatus, Long> counts = new java.util.HashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            counts.put(status, projectService.getProjectCountByStatus(status));
        }
        return ResponseEntity.ok(counts);
    }
}
