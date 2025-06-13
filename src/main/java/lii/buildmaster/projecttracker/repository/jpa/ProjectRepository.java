package lii.buildmaster.projecttracker.repository.jpa;

import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
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

    List<Project> findByNameContainingIgnoreCase(String name);

    List<Project> findByDeadlineBefore(LocalDateTime deadline);

    List<Project> findByStatusAndDeadlineBefore(ProjectStatus status, LocalDateTime deadline);

    @Query("SELECT p FROM Project p WHERE p.deadline < :deadline AND p.status != 'COMPLETED'")
    List<Project> findOverdueProjects(@Param("deadline") LocalDateTime deadline);

    long countByStatus(ProjectStatus status);

    @Query("SELECT p FROM Project p JOIN Task t ON p.id = t.project.id WHERE t.developer.name = :username AND p.status = :status")
    Page<Project> findByStatusAndDeveloperUsername (ProjectStatus status, String username, Pageable pageable);

    @Query("SELECT p FROM Project p JOIN Task t ON p.id = t.project.id WHERE t.developer.name = :username")
    List<Project> findProjectsByDeveloperUsername(String username, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    List<Project> findProjectByStatus(ProjectStatus status);
}
