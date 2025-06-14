package lii.buildmaster.projecttracker.security.expression;

import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.ProjectRepository;
import lii.buildmaster.projecttracker.repository.jpa.TaskRepository;

import lii.buildmaster.projecttracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
public class CustomSecurityExpressions {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final DeveloperRepository developerRepository;


    public boolean canAccessProject(Long projectId) {
        if (SecurityUtils.isAdmin() || SecurityUtils.isManager()) {
            return true;
        }

        if (SecurityUtils.isDeveloper()) {
            return isAssignedToProject(projectId);
        }

        return SecurityUtils.isContractor();
    }

    public boolean canModifyProject(Long projectId) {
        return SecurityUtils.isAdmin() || SecurityUtils.isManager();
    }

    public boolean canCreateProject() {
        return SecurityUtils.isAdmin() || SecurityUtils.isManager();
    }

    public boolean canAccessTask(Long taskId) {
        if (SecurityUtils.isAdmin() || SecurityUtils.isManager()) {
            return true;
        }

        if (SecurityUtils.isDeveloper()) {
            return isAssignedToTask(taskId);
        }

        return false;
    }

    public boolean canModifyTask(Long taskId) {
        if (SecurityUtils.isDeveloper()) {
            return isAssignedToTask(taskId);
        }

        return false;
    }

    public boolean canCreateTask() {
        return SecurityUtils.isAdmin() || SecurityUtils.isManager();
    }

    public boolean canAssignTask() {
        return SecurityUtils.isAdmin() || SecurityUtils.isManager();
    }

    public boolean canUnassignTask() {
        return SecurityUtils.isAdmin() || SecurityUtils.isManager();
    }

    private boolean isAssignedToProject(Long projectId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }

        return projectRepository.findById(projectId)
                .map(project -> project.getTasks().stream()
                        .anyMatch(task -> task != null &&
                                task.getDeveloper() != null &&
                                currentUsername.equals(task.getDeveloper().getUsername())))
                .orElse(false);
    }


    private boolean isAssignedToTask(Long taskId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }

        return taskRepository.findById(taskId)
                .map(task -> task.getDeveloper() != null &&
                        currentUsername.equals(task.getDeveloper().getUsername()))
                .orElse(false);
    }


    public boolean canViewAllDevelopers() {
        return SecurityUtils.isAdmin() || SecurityUtils.isManager();
    }


    public boolean canModifyDeveloper(Long developerId) {
        if (SecurityUtils.isAdmin()) {
            return true;
        }

        if (SecurityUtils.isDeveloper()) {
            return developerRepository.findById(developerId)
                    .map(developer -> developer.getUser() != null &&
                            developer.getUser().getUsername().equals(SecurityUtils.getCurrentUsername()))
                    .orElse(false);
        }

        return false;
    }

    public boolean canManageUsers() {
        return SecurityUtils.isAdmin();
    }

    public boolean canViewAuditLogs() {
        return SecurityUtils.isAdmin();
    }
}