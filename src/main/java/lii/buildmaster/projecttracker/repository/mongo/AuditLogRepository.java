package lii.buildmaster.projecttracker.repository.mongo;

import lii.buildmaster.projecttracker.model.entity.AuditLog;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(EntityType entityType, Pageable pageable);

    List<AuditLog> findByEntityIdOrderByTimestampDesc(String entityId);

    Page<AuditLog> findByActorNameOrderByTimestampDesc(String actorName, Pageable pageable);

    Page<AuditLog> findByActionTypeOrderByTimestampDesc(ActionType actionType, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndActionTypeOrderByTimestampDesc(
            EntityType entityType, ActionType actionType, Pageable pageable);

    @Query("{'timestamp': {'$gte': ?0, '$lte': ?1}}")
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("{'entityType': ?0, 'entityId': ?1}")
    List<AuditLog> findRecentLogsByEntity(EntityType entityType, String entityId, Pageable pageable);

    List<AuditLog> findByCorrelationIdOrderByTimestampDesc(String correlationId);

    long countByActionType(ActionType actionType);

    long countByEntityType(EntityType entityType);

    @Query("{'actorName': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}")
    Page<AuditLog> findByActorAndDateRange(
            String actorName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("{'entityType': ?0, 'entityId': ?1}")
    Page<AuditLog> findEntityAuditTrail(EntityType entityType, String entityId, Pageable pageable);
}