package lii.buildmaster.projecttracker.exception;

import lombok.Getter;

@Getter
public class ProjectNotFoundException extends RuntimeException{

    private final Long projectId;

    public ProjectNotFoundException(Long projectId) {
        super("Project with ID " + projectId + " not found.");
        this.projectId = projectId;
    }
}
