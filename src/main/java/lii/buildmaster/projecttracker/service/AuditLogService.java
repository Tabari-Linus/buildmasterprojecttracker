package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.entity.AuditLog;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    AuditLog logAction(ActionType actionType, EntityType entityType, String entityId,
                       String actorName, Map<String, Object> payload);

    AuditLog logAction(ActionType actionType, EntityType entityType, String entityId,
                       String actorName, Map<String, Object> beforeState, Map<String, Object> afterState);

    AuditLog logActionWithContext(ActionType actionType, EntityType entityType, String entityId,
                                  String actorName, Map<String, Object> payload,
                                  String ipAddress, String userAgent, String sessionId);

    Page<AuditLog> getAllLogs(Pageable pageable);

    Page<AuditLog> getLogsByEntityType(EntityType entityType, Pageable pageable);

    List<AuditLog> getLogsByEntityId(String entityId);

    Page<AuditLog> getLogsByActorName(String actorName, Pageable pageable);

    Page<AuditLog> getLogsByActionType(ActionType actionType, Pageable pageable);

    Page<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<AuditLog> getEntityAuditTrail(EntityType entityType, String entityId, Pageable pageable);

    List<AuditLog> getLogsByCorrelationId(String correlationId);

    long getTotalLogCount();

    long getLogCountByActionType(ActionType actionType);

    long getLogCountByEntityType(EntityType entityType);

    Map<ActionType, Long> getLogCountsByActionType();

    Map<EntityType, Long> getLogCountsByEntityType();

    String generateCorrelationId();

    void cleanupOldLogs(int daysToKeep);
}
