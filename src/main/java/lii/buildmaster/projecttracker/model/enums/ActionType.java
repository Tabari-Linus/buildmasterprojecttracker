package lii.buildmaster.projecttracker.model.enums;

public enum ActionType {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    ASSIGN("Assign"),
    UNASSIGN("Unassign"),
    STATUS_CHANGE("Status Change"),
    LOGIN("Login"),
    LOGOUT("Logout"),
    BULK_UPDATE("Bulk Update"),
    BULK_DELETE("Bulk Delete");

    private final String displayName;

    ActionType(String displayName) {
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
