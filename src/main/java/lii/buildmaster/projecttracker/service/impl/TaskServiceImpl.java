package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.repository.jpa.TaskRepository;
import lii.buildmaster.projecttracker.service.AuditLogService;
import lii.buildmaster.projecttracker.service.TaskService;
import lii.buildmaster.projecttracker.util.AuditUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final AuditLogService auditLogService;
    private final AuditUtil auditUtil;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           DeveloperRepository developerRepository, AuditLogService auditLogService,
                           AuditUtil auditUtil) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
        this.auditLogService = auditLogService;
        this.auditUtil = auditUtil;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true)
    })
    public Task createTask(String title, String description, TaskStatus status, LocalDateTime dueDate, Long projectId, Long developerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Developer developer = null;
        if (developerId != null) {
            developer = developerRepository.findById(developerId)
                    .orElseThrow(() -> new RuntimeException("Developer not found with id: " + developerId));
        }

        Task task = new Task(title, description, status, dueDate, project, developer);
        Task savedTask = taskRepository.save(task);

        Map<String, Object> payload = auditUtil.createTaskAuditPayload(savedTask);
        auditLogService.logAction(
                ActionType.CREATE,
                EntityType.TASK,
                savedTask.getId().toString(),
                auditUtil.getCurrentActorName(),
                payload
        );

        return savedTask;
    }

    @Override
    public Task createTask(String title, String description, TaskStatus status, LocalDateTime dueDate, Long projectId) {
        return createTask(title, description, status, dueDate, projectId, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'all'")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#id")
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#id"),
            @CacheEvict(value = "tasks", key = "'all'"),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public Task updateTask(Long id, String title, String description, TaskStatus status, LocalDateTime dueDate) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        Map<String, Object> beforeState = auditUtil.createTaskAuditPayload(task);

        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);

        Task updatedTask = taskRepository.save(task);
        Map<String, Object> afterState = auditUtil.createTaskAuditPayload(updatedTask);

        auditLogService.logAction(
                ActionType.UPDATE,
                EntityType.TASK,
                updatedTask.getId().toString(),
                auditUtil.getCurrentActorName(),
                beforeState,
                afterState
        );

        return updatedTask;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true)
    })
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        Map<String, Object> payload = auditUtil.createTaskAuditPayload(task);

        taskRepository.deleteById(id);

        auditLogService.logAction(
                ActionType.DELETE,
                EntityType.TASK,
                id.toString(),
                auditUtil.getCurrentActorName(),
                payload
        );
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public Task assignTaskToDeveloper(Long taskId, Long developerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer not found with id: " + developerId));

        Developer oldDeveloper = task.getDeveloper();
        task.setDeveloper(developer);
        Task savedTask = taskRepository.save(task);

        Map<String, Object> payload = auditUtil.createAssignmentAuditPayload(savedTask, oldDeveloper, developer);
        auditLogService.logAction(
                ActionType.ASSIGN,
                EntityType.TASK,
                savedTask.getId().toString(),
                auditUtil.getCurrentActorName(),
                payload
        );
        return savedTask;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public Task unassignTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        Developer oldDeveloper = task.getDeveloper();
        task.setDeveloper(null);
        Task savedTask = taskRepository.save(task);

        Map<String, Object> payload = auditUtil.createAssignmentAuditPayload(savedTask, oldDeveloper, null);
        auditLogService.logAction(
                ActionType.UNASSIGN,
                EntityType.TASK,
                savedTask.getId().toString(),
                auditUtil.getCurrentActorName(),
                payload
        );

        return savedTask;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'project_' + #projectId")
    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'developer_' + #developerId")
    public List<Task> getTasksByDeveloper(Long developerId) {
        return taskRepository.findByDeveloperId(developerId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'unassigned'")
    public List<Task> getUnassignedTasks() {
        return taskRepository.findByDeveloperIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'status_' + #status.name()")
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'project_' + #projectId + '_status_' + #status.name()")
    public List<Task> getTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        return taskRepository.findByProjectIdAndStatus(projectId, status);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'developer_' + #developerId + '_status_' + #status.name()")
    public List<Task> getTasksByDeveloperAndStatus(Long developerId, TaskStatus status) {
        return taskRepository.findByDeveloperIdAndStatus(developerId, status);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'overdue'")
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'due_within_' + #days")
    public List<Task> getTasksDueWithinDays(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(days);
        return taskRepository.findTasksDueWithinDays(now, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'search_' + #title")
    public List<Task> searchTasksByTitle(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true)
    })
    public Task markTaskAsCompleted(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        Map<String, Object> beforeState = auditUtil.createTaskAuditPayload(task);

        task.setStatus(TaskStatus.DONE);

        Task updatedTask = taskRepository.save(task);
        Map<String, Object> afterState = auditUtil.createTaskAuditPayload(updatedTask);

        auditLogService.logAction(
                ActionType.UPDATE,
                EntityType.TASK,
                updatedTask.getId().toString(),
                auditUtil.getCurrentActorName(),
                beforeState,
                afterState
        );

        return updatedTask;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public Task moveTaskToInProgress(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        Map<String, Object> beforeState = auditUtil.createTaskAuditPayload(task);

        task.setStatus(TaskStatus.IN_PROGRESS);

        Task updatedTask = taskRepository.save(task);
        Map<String, Object> afterState = auditUtil.createTaskAuditPayload(updatedTask);

        auditLogService.logAction(
                ActionType.UPDATE,
                EntityType.TASK,
                updatedTask.getId().toString(),
                auditUtil.getCurrentActorName(),
                beforeState,
                afterState
        );

        return updatedTask;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskStats", key = "'count_status_' + #status.name()")
    public long getTaskCountByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskStats", key = "'count_project_' + #projectId")
    public long getTaskCountByProject(Long projectId) {
        return taskRepository.countByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskStats", key = "'count_developer_' + #developerId")
    public long getTaskCountByDeveloper(Long developerId) {
        return taskRepository.countByDeveloperId(developerId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskStats", key = "'counts_by_status'")
    public Map<TaskStatus, Long> getTaskCountsByStatus() {
        List<Object[]> results = taskRepository.getTaskCountsByStatus();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (TaskStatus) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskStats", key = "'top_developers_' + #limit")
    public List<Map<String, Object>> getTopDevelopersWithMostTasks(int limit) {
        List<Object[]> results = taskRepository.findTopDevelopersWithMostTasks(PageRequest.of(0, limit));

        return results.stream()
                .map(result -> {
                    Map<String, Object> developerInfo = new HashMap<>();
                    developerInfo.put("developerId", result[0]);
                    developerInfo.put("developerName", result[1]);
                    developerInfo.put("taskCount", result[2]);
                    return developerInfo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'overdue_projects'")
    public List<Task> getTasksInOverdueProjects() {
        return taskRepository.findTasksInOverdueProjects(LocalDateTime.now());
    }
}
