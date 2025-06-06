package lii.buildmaster.projecttracker.exception;

import lombok.Getter;

@Getter
public class TaskNotFoundException extends RuntimeException {

    private final long taskId;

    public TaskNotFoundException(long taskId) {
        super("Task with ID " + taskId + " not found.");
        this.taskId = taskId;
    }

}