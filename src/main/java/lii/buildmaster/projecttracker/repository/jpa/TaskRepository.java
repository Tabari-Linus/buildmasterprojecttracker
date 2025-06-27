package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
// import org.springframework.data.jpa.repository.EntityGraph; // Temporarily remove this import

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // IMPORTANT: Add this import if not present

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

    // --- REMOVED PROBLEMatic @EntityGraph METHODS FOR NOW ---
    // Will re-introduce or find alternative eager fetching strategies later if necessary.

    // @EntityGraph(attributePaths = {"project"})
    // @Query("SELECT t FROM Task t")
    // List<Task> findAllWithProject();

    // @EntityGraph(attributePaths = {"project", "developer"})
    // @Query("SELECT t FROM Task t WHERE t.id = :id")
    // Optional<Task> findByIdWithProjectAndDeveloper(@Param("id") Long id);

    // @EntityGraph(attributePaths = {"developer"})
    // @Query("SELECT t FROM Task t WHERE t.project.id = :projectId")
    // List<Task> findByProjectIdWithDeveloper(@Param("projectId") Long projectId);

    // @EntityGraph(attributePaths = {"project"})
    // @Query("SELECT t FROM Task t WHERE t.developer.id = :developerId")
    // List<Task> findByDeveloperIdWithProject(@Param("developerId") Long developerId);

    // @EntityGraph(attributePaths = {"project", "developer"})
    // @Query("SELECT t FROM Task t")
    // Page<Task> findAllWithProjectAndDeveloper(Pageable pageable);
}