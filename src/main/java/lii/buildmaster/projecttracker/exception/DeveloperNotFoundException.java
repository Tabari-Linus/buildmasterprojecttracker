package lii.buildmaster.projecttracker.exception;

public class DeveloperNotFoundException extends RuntimeException{

    private final long developerId;

    public DeveloperNotFoundException(long developerId) {
        super("Develops with ID " + developerId + " not found.");
        this.developerId = developerId;
    }
}
