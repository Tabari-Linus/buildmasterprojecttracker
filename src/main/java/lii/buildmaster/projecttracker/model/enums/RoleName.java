package lii.buildmaster.projecttracker.model.enums;

public enum RoleName {
    ROLE_ADMIN("Has system administration privileges and can manage users and view audit logs"),
    ROLE_MANAGER("Manager who can create and manage projects and tasks"),
    ROLE_DEVELOPER("Developer who  can view and update only the tasks assigned to them"),
    ROLE_CONTRACTOR("External contractor can only have read-only access unless approved by an Admin.");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
