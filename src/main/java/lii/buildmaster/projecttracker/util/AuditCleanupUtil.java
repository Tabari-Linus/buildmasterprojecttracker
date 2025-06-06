package lii.buildmaster.projecttracker.util;

import lii.buildmaster.projecttracker.repository.mongo.AuditLogRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditCleanupUtil {

    private final AuditLogRepository auditLogRepository;

    public AuditCleanupUtil(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void clearAllAuditLogs() {
        long count = auditLogRepository.count();
        auditLogRepository.deleteAll();
        System.out.println("Cleared " + count + " audit logs from MongoDB");
    }

    public long getAuditLogCount() {
        return auditLogRepository.count();
    }
}
