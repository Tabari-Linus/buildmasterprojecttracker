package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(ProjectStatus status);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    List<Project> findByNameContainingIgnoreCase(String name);

    List<Project> findByDeadlineBefore(LocalDateTime deadline);

    @Query("SELECT p FROM Project p WHERE p.deadline < :deadline AND p.status != 'COMPLETED'")
    List<Project> findOverdueProjects(@Param("deadline") LocalDateTime deadline);

    long countByStatus(ProjectStatus status);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.tasks t WHERE t.developer.name = :username AND p.status = :status")
    Page<Project> findByStatusAndDeveloperUsername(@Param("status") ProjectStatus status,
                                                   @Param("username") String username,
                                                   Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.tasks t WHERE t.developer.name = :username")
    List<Project> findProjectsByDeveloperUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> getProjectCountsByStatus();
}
