package lii.buildmaster.projecttracker.controller.v1;

import lii.buildmaster.projecttracker.model.dto.request.TaskRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.TaskResponseDto;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lii.buildmaster.projecttracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskControllerV1 {

    private final TaskService taskService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DEVELOPER')")
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }


    @GetMapping("/{id}")
    @PreAuthorize("@security.canAccessTask(#id)")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    @PreAuthorize("@security.canCreateTask()")
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto taskRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(taskRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@security.canModifyTask(#id)")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto taskRequest) {
        return ResponseEntity.ok(taskService.updateTask(id,taskRequest.getTitle(),
                taskRequest.getDescription(),
                taskRequest.getStatus(),
                taskRequest.getDueDate()));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{taskId}/assign/{developerId}")
    @PreAuthorize("@security.canAssignTask()")
    public ResponseEntity<Void> assignTaskToDeveloper(
            @PathVariable Long taskId,
            @PathVariable Long developerId) {
        taskService.assignTaskToDeveloper(taskId, developerId);
        return ResponseEntity.ok().build();
    }
}