package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.model.entity.AuditLog;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.repository.mongo.AuditLogRepository;
import lii.buildmaster.projecttracker.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AuditLog logAction(ActionType actionType, EntityType entityType, String entityId,
                              String actorName, Map<String, Object> payload) {
        AuditLog auditLog = new AuditLog(actionType, entityType, entityId, actorName, payload);
        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog logAction(ActionType actionType, EntityType entityType, String entityId,
                              String actorName, Map<String, Object> beforeState, Map<String, Object> afterState) {
        AuditLog auditLog = new AuditLog(actionType, entityType, entityId, actorName, beforeState, afterState);
        return auditLogRepository.save(auditLog);
    }


    @Override
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public Page<AuditLog> getLogsByEntityType(EntityType entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType, pageable);
    }

    @Override
    public List<AuditLog> getLogsByEntityId(String entityId) {
        return auditLogRepository.findByEntityIdOrderByTimestampDesc(entityId);
    }

    @Override
    public Page<AuditLog> getLogsByActorName(String actorName, Pageable pageable) {
        return auditLogRepository.findByActorNameOrderByTimestampDesc(actorName, pageable);
    }

    @Override
    public Page<AuditLog> getLogsByActionType(ActionType actionType, Pageable pageable) {
        return auditLogRepository.findByActionTypeOrderByTimestampDesc(actionType, pageable);
    }

    @Override
    public Page<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate, pageable);
    }

    @Override
    public Page<AuditLog> getEntityAuditTrail(EntityType entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findEntityAuditTrail(entityType, entityId, pageable);
    }

    @Override
    public List<AuditLog> getLogsByCorrelationId(String correlationId) {
        return auditLogRepository.findByCorrelationIdOrderByTimestampDesc(correlationId);
    }

    @Override
    public long getTotalLogCount() {
        return auditLogRepository.count();
    }

    @Override
    public long getLogCountByActionType(ActionType actionType) {
        return auditLogRepository.countByActionType(actionType);
    }

    @Override
    public long getLogCountByEntityType(EntityType entityType) {
        return auditLogRepository.countByEntityType(entityType);
    }

    @Override
    public Map<ActionType, Long> getLogCountsByActionType() {
        return Arrays.stream(ActionType.values())
                .collect(Collectors.toMap(
                        actionType -> actionType,
                        this::getLogCountByActionType
                ));
    }

    @Override
    public Map<EntityType, Long> getLogCountsByEntityType() {
        return Arrays.stream(EntityType.values())
                .collect(Collectors.toMap(
                        entityType -> entityType,
                        this::getLogCountByEntityType
                ));
    }

}