package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByDeveloperId(Long developerId);

    List<Task> findByDeveloperIsNull();

    List<Task> findByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentTime AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startTime AND :endTime AND t.status != 'DONE'")
    List<Task> findTasksDueWithinDays(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Task> findByTitleContainingIgnoreCase(String title);

    long countByStatus(TaskStatus status);

    long countByProjectId(Long projectId);

    long countByDeveloperId(Long developerId);

    @Query("SELECT t.developer.id, t.developer.name, COUNT(t) as taskCount " +
            "FROM Task t WHERE t.developer IS NOT NULL " +
            "GROUP BY t.developer.id, t.developer.name " +
            "ORDER BY taskCount DESC")
    List<Object[]> findTopDevelopersWithMostTasks(Pageable pageable);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> getTaskCountsByStatus();

    @Query("SELECT p FROM Project p WHERE p.id NOT IN (SELECT DISTINCT t.project.id FROM Task t)")
    List<Object[]> findProjectsWithoutTasks();

    @Query("SELECT t FROM Task t WHERE t.project.deadline < :currentTime AND t.status != 'DONE'")
    List<Task> findTasksInOverdueProjects(@Param("currentTime") LocalDateTime currentTime);

    long countByDeveloperIdAndStatus(Long id, TaskStatus taskStatus);

    long countByDeveloperIdAndStatusIn(Long id, Set<TaskStatus> todo);

    long countByProjectIdAndStatus(Long id, TaskStatus taskStatus);
}