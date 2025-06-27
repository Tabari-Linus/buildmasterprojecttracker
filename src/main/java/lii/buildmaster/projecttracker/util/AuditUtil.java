package lii.buildmaster.projecttracker.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.entity.Task;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuditUtil {

    public AuditUtil() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }


    public Map<String, Object> createProjectAuditPayload(Project project) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", project.getId());
        payload.put("name", project.getName());
        payload.put("description", project.getDescription());
        payload.put("deadline", project.getDeadline());
        payload.put("status", project.getStatus());
        payload.put("taskCount", project.getTasks() != null ? project.getTasks().size() : 0);
        return payload;
    }

    public Map<String, Object> createDeveloperAuditPayload(Developer developer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", developer.getId());
        payload.put("name", developer.getName());
        payload.put("email", developer.getEmail());
        payload.put("skills", developer.getSkills());
        payload.put("taskCount", developer.getAssignedTasks() != null ? developer.getAssignedTasks().size() : 0);
        return payload;
    }

    public Map<String, Object> createTaskAuditPayload(Task task) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", task.getId());
        payload.put("title", task.getTitle());
        payload.put("description", task.getDescription());
        payload.put("status", task.getStatus());
        payload.put("dueDate", task.getDueDate());
        payload.put("projectId", task.getProject() != null ? task.getProject().getId() : null);
        payload.put("projectName", task.getProject() != null ? task.getProject().getName() : null);
        payload.put("developerId", task.getDeveloper() != null ? task.getDeveloper().getId() : null);
        payload.put("developerName", task.getDeveloper() != null ? task.getDeveloper().getName() : null);
        return payload;
    }


    public String getCurrentActorName() {
        return "System";
    }

    public Map<String, Object> createAssignmentAuditPayload(Task task, Developer oldDeveloper, Developer newDeveloper) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", task.getId());
        payload.put("taskTitle", task.getTitle());
        payload.put("oldDeveloperId", oldDeveloper != null ? oldDeveloper.getId() : null);
        payload.put("oldDeveloperName", oldDeveloper != null ? oldDeveloper.getName() : null);
        payload.put("newDeveloperId", newDeveloper != null ? newDeveloper.getId() : null);
        payload.put("newDeveloperName", newDeveloper != null ? newDeveloper.getName() : null);
        return payload;
    }

}
