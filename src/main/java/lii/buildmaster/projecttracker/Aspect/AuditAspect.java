package lii.buildmaster.projecttracker.Aspect;

import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.service.AuditLogService;
import lii.buildmaster.projecttracker.util.AuditUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final AuditUtil auditUtil;

    public AuditAspect(AuditLogService auditLogService, AuditUtil auditUtil) {
        this.auditLogService = auditLogService;
        this.auditUtil = auditUtil;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAuditableAction(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String entityId = extractEntityId(result, auditable);
            Map<String, Object> payload = createPayload(result, auditable);

            auditLogService.logAction(
                    auditable.action(),
                    auditable.entityType(),
                    entityId,
                    auditUtil.getCurrentActorName(),
                    payload
            );

        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    private String extractEntityId(Object result, Auditable auditable) {
        if (result instanceof Project) {
            return ((Project) result).getId().toString();
        } else if (result instanceof Developer) {
            return ((Developer) result).getId().toString();
        } else if (result instanceof Task) {
            return ((Task) result).getId().toString();
        }
        return "unknown";
    }

    private Map<String, Object> createPayload(Object result, Auditable auditable) {
        if (result instanceof Project) {
            return auditUtil.createProjectAuditPayload((Project) result);
        } else if (result instanceof Developer) {
            return auditUtil.createDeveloperAuditPayload((Developer) result);
        } else if (result instanceof Task) {
            return auditUtil.createTaskAuditPayload((Task) result);
        }
        return Map.of("result", result.toString());
    }
}
