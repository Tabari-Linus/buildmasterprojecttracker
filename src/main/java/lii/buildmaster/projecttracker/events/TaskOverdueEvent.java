package lii.buildmaster.projecttracker.events;

import lii.buildmaster.projecttracker.model.entity.Task;
import org.springframework.context.ApplicationEvent;

public class TaskOverdueEvent extends ApplicationEvent {
    private final Task task;
    private final String recipientEmail;
    private final String message;

    public TaskOverdueEvent(Object source, Task task, String recipientEmail, String message) {
        super(source);
        this.task = task;
        this.recipientEmail = recipientEmail;
        this.message = message;
    }

    public Task getTask() {
        return task;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getMessage() {
        return message;
    }
}
