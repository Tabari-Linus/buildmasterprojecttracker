package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private JavaMailSender mailSender;

    @Override
    public void sendTaskOverdueNotification(String to, String taskName, String projectName, String dueDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Task Overdue - " + taskName);
            message.setText(buildOverdueTaskEmailBody(taskName, projectName, dueDate));
            message.setFrom("noreply@buildmater.com");

            mailSender.send(message);
            logger.info("Overdue task notification sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send overdue task notification to: {}", to, e);
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@buildmater.com");

            mailSender.send(message);
            logger.info("Email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
        }
    }

    private String buildOverdueTaskEmailBody(String taskName, String projectName, String dueDate) {
        return String.format(
                "Dear Team Member,\n\n" +
                        "This is to notify you that the following task is overdue:\n\n" +
                        "Task: %s\n" +
                        "Project: %s\n" +
                        "Due Date: %s\n\n" +
                        "Please take immediate action to complete this task.\n\n" +
                        "Best regards,\n" +
                        "BuildMater Team",
                taskName, projectName, dueDate
        );
    }
}
