package lii.buildmaster.projecttracker.service;

public interface EmailService {
    void sendTaskOverdueNotification(String to, String taskName, String projectName, String dueDate);
    void sendSimpleEmail(String to, String subject, String body);
}
