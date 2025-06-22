package lii.buildmaster.projecttracker.exception;

import lombok.Getter;

@Getter
public class DeveloperNotFoundException extends RuntimeException{

    private final Long developerId;

    public DeveloperNotFoundException(Long developerId) {
        super("Develops with ID " + developerId + " not found.");
        this.developerId = developerId;
    }

    public DeveloperNotFoundException(String s) {
        super(s);
        this.developerId = null;
    }
}
