package lii.buildmaster.projecttracker.controller.vi;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lii.buildmaster.projecttracker.mapper.TaskMapper;
import lii.buildmaster.projecttracker.model.dto.request.TaskAssignmentDto;
import lii.buildmaster.projecttracker.model.dto.request.TaskRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.TaskResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.TaskSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lii.buildmaster.projecttracker.service.impl.TaskServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task management operations including CRUD, assignment workflows, status tracking, and analytics")
public class TaskControllerV1 {

    private final TaskServiceImpl taskServiceImpl;
    private final TaskMapper taskMapper;

    public TaskControllerV1(TaskServiceImpl taskServiceImpl, TaskMapper taskMapper) {
        this.taskServiceImpl = taskServiceImpl;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public ResponseEntity<Page<TaskSummaryDto>> getAllTasks(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        List<Task> tasks = taskServiceImpl.getAllTasks();
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), taskDtos.size());
        List<TaskSummaryDto> pageContent = taskDtos.subList(start, end);

        Page<TaskSummaryDto> page = new PageImpl<>(pageContent, pageable, taskDtos.size());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        Task task = taskServiceImpl.getTaskById(id);
        return ResponseEntity.ok(taskMapper.toResponseDto(task));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto requestDto) {
        try {
            Task task = taskServiceImpl.createTask(
                    requestDto.getTitle(),
                    requestDto.getDescription(),
                    requestDto.getStatus(),
                    requestDto.getDueDate(),
                    requestDto.getProjectId(),
                    requestDto.getDeveloperId()
            );

            TaskResponseDto responseDto = taskMapper.toResponseDto(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto requestDto) {

        try {
            Task updatedTask = taskServiceImpl.updateTask(
                    id,
                    requestDto.getTitle(),
                    requestDto.getDescription(),
                    requestDto.getStatus(),
                    requestDto.getDueDate()
            );

            TaskResponseDto responseDto = taskMapper.toResponseDto(updatedTask);
            return ResponseEntity.ok(responseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskServiceImpl.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskResponseDto> assignTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskAssignmentDto assignmentDto) {

        try {
            Task task = taskServiceImpl.assignTaskToDeveloper(id, assignmentDto.getDeveloperId());
            TaskResponseDto responseDto = taskMapper.toResponseDto(task);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/unassign")
    public ResponseEntity<TaskResponseDto> unassignTask(@PathVariable Long id) {
        try {
            Task task = taskServiceImpl.unassignTask(id);
            TaskResponseDto responseDto = taskMapper.toResponseDto(task);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<TaskResponseDto> markAsCompleted(@PathVariable Long id) {
        try {
            Task task = taskServiceImpl.markTaskAsCompleted(id);
            TaskResponseDto responseDto = taskMapper.toResponseDto(task);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<TaskResponseDto> moveToInProgress(@PathVariable Long id) {
        try {
            Task task = taskServiceImpl.moveTaskToInProgress(id);
            TaskResponseDto responseDto = taskMapper.toResponseDto(task);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskSummaryDto>> getTasksByProject(@PathVariable Long projectId) {
        List<Task> tasks = taskServiceImpl.getTasksByProject(projectId);
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<List<TaskSummaryDto>> getTasksByDeveloper(@PathVariable Long developerId) {
        List<Task> tasks = taskServiceImpl.getTasksByDeveloper(developerId);
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<TaskSummaryDto>> getUnassignedTasks() {
        List<Task> tasks = taskServiceImpl.getUnassignedTasks();
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/status")
    public ResponseEntity<List<TaskSummaryDto>> getTasksByStatus(@RequestParam TaskStatus status) {
        List<Task> tasks = taskServiceImpl.getTasksByStatus(status);
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskSummaryDto>> getOverdueTasks() {
        List<Task> tasks = taskServiceImpl.getOverdueTasks();
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/due-within/{days}")
    public ResponseEntity<List<TaskSummaryDto>> getTasksDueWithinDays(@PathVariable int days) {
        List<Task> tasks = taskServiceImpl.getTasksDueWithinDays(days);
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TaskSummaryDto>> searchTasks(@RequestParam String title) {
        List<Task> tasks = taskServiceImpl.searchTasksByTitle(title);
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/overdue-projects")
    public ResponseEntity<List<TaskSummaryDto>> getTasksInOverdueProjects() {
        List<Task> tasks = taskServiceImpl.getTasksInOverdueProjects();
        List<TaskSummaryDto> taskDtos = tasks.stream()
                .map(taskMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/stats/count-by-status")
    public ResponseEntity<Map<TaskStatus, Long>> getTaskCountsByStatus() {
        Map<TaskStatus, Long> counts = taskServiceImpl.getTaskCountsByStatus();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/stats/top-developers")
    public ResponseEntity<List<Map<String, Object>>> getTopDevelopers(
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> topDevelopers = taskServiceImpl.getTopDevelopersWithMostTasks(limit);
        return ResponseEntity.ok(topDevelopers);
    }

    @GetMapping("/stats/project/{projectId}/count")
    public ResponseEntity<Map<String, Long>> getTaskCountByProject(@PathVariable Long projectId) {
        long count = taskServiceImpl.getTaskCountByProject(projectId);
        Map<String, Long> response = Map.of("projectId", projectId, "taskCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/developer/{developerId}/count")
    public ResponseEntity<Map<String, Long>> getTaskCountByDeveloper(@PathVariable Long developerId) {
        long count = taskServiceImpl.getTaskCountByDeveloper(developerId);
        Map<String, Long> response = Map.of("developerId", developerId, "taskCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects-without-task")
    public ResponseEntity<List<Project>> getProjectsWithoutTasks(){
        return ResponseEntity.ok(taskServiceImpl.getProjectsWithoutTasks());
    }
}
