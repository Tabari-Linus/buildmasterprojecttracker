package lii.buildmaster.projecttracker.model.entity;

import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    @Field("action_type")
    private ActionType actionType;

    @Field("entity_type")
    private EntityType entityType;

    @Field("entity_id")
    private String entityId;

    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("actor_name")
    private String actorName;

    @Field("actor_id")
    private String actorId;

    @Field("payload")
    private Map<String, Object> payload;

    @Field("before_state")
    private Map<String, Object> beforeState;

    @Field("after_state")
    private Map<String, Object> afterState;

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("session_id")
    private String sessionId;

    @Field("correlation_id")
    private String correlationId;

    public AuditLog(ActionType actionType, EntityType entityType, String entityId,
                    String actorName, Map<String, Object> payload) {
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actorName = actorName;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(ActionType actionType, EntityType entityType, String entityId,
                    String actorName, Map<String, Object> beforeState, Map<String, Object> afterState) {
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actorName = actorName;
        this.beforeState = beforeState;
        this.afterState = afterState;
        this.timestamp = LocalDateTime.now();
    }
}
