package lii.buildmaster.projecttracker.model.enums;

public enum EntityType {
    PROJECT("Project"),
    DEVELOPER("Developer"),
    TASK("Task"),
    USER("User"),
    SYSTEM("System");

    private final String displayName;

    EntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
