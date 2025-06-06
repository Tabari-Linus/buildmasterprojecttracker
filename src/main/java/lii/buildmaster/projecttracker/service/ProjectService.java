package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectService {

    Project createProject(String name, String description, LocalDateTime deadline, ProjectStatus status);

    List<Project> getAllProjects();

    Project getProjectById(Long id);

    List<Project> getProjectsByStatus(ProjectStatus status);

    Project updateProject(Long id, String name, String description, LocalDateTime deadline, ProjectStatus status);

    void deleteProject(Long id);

    List<Project> getOverdueProjects();

    long getProjectCountByStatus(ProjectStatus status);

    List<Project> searchProjectsByName(String name);

    Project markAsCompleted(Long id);
}
