package lii.buildmaster.projecttracker.exception;

import lombok.Getter;

@Getter
public class ProjectNotFoundException extends RuntimeException{

    private final long projectId;

    public ProjectNotFoundException(long projectId) {
        super("Project with ID " + projectId + " not found.");
        this.projectId = projectId;
    }
}
