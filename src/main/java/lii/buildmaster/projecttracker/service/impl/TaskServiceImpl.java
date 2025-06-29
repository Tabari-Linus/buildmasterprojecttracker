package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.exception.DeveloperNotFoundException;
import lii.buildmaster.projecttracker.exception.ProjectNotFoundException;
import lii.buildmaster.projecttracker.exception.TaskNotFoundException;
import lii.buildmaster.projecttracker.mapper.ProjectMapper;
import lii.buildmaster.projecttracker.mapper.TaskMapper;
import lii.buildmaster.projecttracker.mapper.DeveloperMapper;
import lii.buildmaster.projecttracker.model.dto.request.TaskRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.TaskResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.TaskSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.repository.jpa.TaskRepository;
import lii.buildmaster.projecttracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;
    private final DeveloperMapper developerMapper;

    @Override
    @Auditable(action = ActionType.CREATE, entityType = EntityType.TASK)
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true)
    })
    public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
        Project project = projectRepository.findById(taskRequestDto.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(taskRequestDto.getProjectId()));
        Developer developer = null;
        if (taskRequestDto.getDeveloperId() != null) {
            developer = developerRepository.findById(taskRequestDto.getDeveloperId())
                    .orElseThrow(() -> new DeveloperNotFoundException(taskRequestDto.getDeveloperId()));
        }

        Task task = taskMapper.toEntity(taskRequestDto);
        task.setProject(project);
        task.setDeveloper(developer);

        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponseDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasks(Pageable pageable) {
        Page<Task> tasksPage = taskRepository.findAll(pageable);
        List<TaskResponseDto> taskResponseDtos = tasksPage.getContent().stream()
                .map(this::mapTaskToResponseDtoWithCalculatedFields)
                .collect(Collectors.toList());
        return new PageImpl<>(taskResponseDtos, pageable, tasksPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "'all'")
    public List<Task> getAllTask() {


        return taskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#id")
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return mapTaskToResponseDtoWithCalculatedFields(task);
    }

    @Override
    @Auditable(action = ActionType.UPDATE, entityType = EntityType.TASK)
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#id"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public TaskResponseDto updateTask(Long id, String title, String description, TaskStatus status, LocalDateTime dueDate) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);

        Task updatedTask = taskRepository.save(task);
        return mapTaskToResponseDtoWithCalculatedFields(updatedTask);
    }

    @Override
    @Auditable(action = ActionType.DELETE, entityType = EntityType.TASK)
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true)
    })
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.deleteById(id);
    }

    @Override
    @Auditable(action = ActionType.ASSIGN, entityType = EntityType.TASK)
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public Task assignTaskToDeveloper(Long taskId, Long developerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new DeveloperNotFoundException(developerId));

        task.setDeveloper(developer);
        return taskRepository.save(task);
    }

    @Override
    @Auditable(action = ActionType.UNASSIGN, entityType = EntityType.TASK)
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public Task unassignTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setDeveloper(null);
        return taskRepository.save(task);
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
    @Transactional
    @Cacheable(value = "projects", key = "'projects_with_no_task'")
    public List<Project> getProjectsWithoutTasks(){
        List<Object[]> results = taskRepository.findProjectsWithoutTasks();
        return results.stream()
                .map( p ->{
                    Project project = new Project();
                    project.setId((Long) p[0]);
                    project.setName((String) p[1]);
                    project.setDescription((String) p[2]);
                    return project;
                }).collect(Collectors.toList());
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
    @Auditable(action = ActionType.STATUS_CHANGE, entityType = EntityType.TASK)
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "developers", allEntries = true)
    })
    public Task markTaskAsCompleted(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setStatus(TaskStatus.DONE);

        return taskRepository.save(task);
    }

    @Override
    @Auditable(action = ActionType.STATUS_CHANGE, entityType = EntityType.TASK)
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#taskId"),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public Task moveTaskToInProgress(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setStatus(TaskStatus.IN_PROGRESS);

        return taskRepository.save(task);
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


    private TaskResponseDto mapTaskToResponseDtoWithCalculatedFields(Task task) {
        TaskResponseDto dto = taskMapper.toResponseDto(task);

        if (task.getDueDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            dto.setOverdue(task.getDueDate().isBefore(now));
            Duration duration = Duration.between(now, task.getDueDate());
            dto.setDaysUntilDue(duration.toDays());
        } else {
            dto.setOverdue(false);
            dto.setDaysUntilDue(0L);
        }
        return dto;
    }


    private TaskSummaryDto mapTaskToSummaryDtoWithCalculatedFields(Task task) {
        TaskSummaryDto dto = taskMapper.toSummaryDto(task);

        if (task.getDueDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            dto.setOverdue(task.getDueDate().isBefore(now));
        } else {
            dto.setOverdue(false);
        }
        return dto;
    }
}