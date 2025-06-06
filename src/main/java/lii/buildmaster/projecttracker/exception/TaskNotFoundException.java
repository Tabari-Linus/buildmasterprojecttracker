package lii.buildmaster.projecttracker.exception;

import lombok.Getter;

@Getter
public class TaskNotFoundException extends RuntimeException {

    private final Long taskId;

    public TaskNotFoundException(Long taskId) {
        super("Task with ID " + taskId + " not found.");
        this.taskId = taskId;
    }

}