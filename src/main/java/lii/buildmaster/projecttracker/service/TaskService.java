package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {

    Task createTask(String title, String description, TaskStatus status, LocalDateTime dueDate, Long projectId, Long developerId);

    void createTask(String title, String description, TaskStatus status, LocalDateTime dueDate, Long projectId);

    List<Task> getAllTasks();

    Optional<Task> getTaskById(Long id);

    Task updateTask(Long id, String title, String description, TaskStatus status, LocalDateTime dueDate);

    void deleteTask(Long id);

    Task assignTaskToDeveloper(Long taskId, Long developerId);

    Task unassignTask(Long taskId);

    List<Task> getTasksByProject(Long projectId);

    List<Task> getTasksByDeveloper(Long developerId);

    List<Task> getUnassignedTasks();

    List<Task> getTasksByStatus(TaskStatus status);

    List<Project> getProjectsWithoutTasks();

    List<Task> getOverdueTasks();

    List<Task> getTasksDueWithinDays(int days);

    List<Task> searchTasksByTitle(String title);

    Task markTaskAsCompleted(Long taskId);

    Task moveTaskToInProgress(Long taskId);

    long getTaskCountByStatus(TaskStatus status);

    long getTaskCountByProject(Long projectId);

    long getTaskCountByDeveloper(Long developerId);

    Map<TaskStatus, Long> getTaskCountsByStatus();

    List<Map<String, Object>> getTopDevelopersWithMostTasks(int limit);

    List<Task> getTasksInOverdueProjects();
}
