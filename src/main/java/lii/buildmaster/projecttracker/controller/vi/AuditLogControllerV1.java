package lii.buildmaster.projecttracker.controller.vi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lii.buildmaster.projecttracker.model.entity.AuditLog;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit-logs")
@CrossOrigin(origins = "*")
@Tag(name = "Audit Logs", description = "AUdit Logger for tacking user activities and changes to entities")
public class AuditLogControllerV1 {

    private final AuditLogService auditLogService;

    public AuditLogControllerV1(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(
            summary = "Get all audit logs with pagination",
            description = "Retrieve a paginated list of all audit logs ordered by timestamp (newest first)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllLogs(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        Page<AuditLog> logs = auditLogService.getAllLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @Operation(
            summary = "Get audit logs by entity type",
            description = "Retrieve audit logs for a specific entity type (e.g., PROJECT, DEVELOPER, TASK)"
    )
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<Page<AuditLog>> getLogsByEntityType(
            @Parameter(description = "Entity type", required = true,
                    schema = @Schema(implementation = EntityType.class))
            @PathVariable EntityType entityType,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditLogService.getLogsByEntityType(entityType, pageable);
        return ResponseEntity.ok(logs);
    }

    @Operation(
            summary = "Get audit logs by entity ID",
            description = "Retrieve complete audit trail for a specific entity"
    )
    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<AuditLog>> getLogsByEntityId(
            @Parameter(description = "Entity ID", required = true, example = "1")
            @PathVariable String entityId) {
        List<AuditLog> logs = auditLogService.getLogsByEntityId(entityId);
        return ResponseEntity.ok(logs);
    }

    @Operation(
            summary = "Get complete audit trail for entity",
            description = "Retrieve paginated audit trail for a specific entity type and ID"
    )
    @GetMapping("/trail/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLog>> getEntityAuditTrail(
            @Parameter(description = "Entity type", required = true)
            @PathVariable EntityType entityType,
            @Parameter(description = "Entity ID", required = true, example = "1")
            @PathVariable String entityId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AuditLog> trail = auditLogService.getEntityAuditTrail(entityType, entityId, pageable);
        return ResponseEntity.ok(trail);
    }

    @Operation(
            summary = "Get audit logs by actor name",
            description = "Retrieve all actions performed by a specific user/actor"
    )
    @GetMapping("/actor/{actorName}")
    public ResponseEntity<Page<AuditLog>> getLogsByActor(
            @Parameter(description = "Actor/user name", required = true, example = "System")
            @PathVariable String actorName,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditLogService.getLogsByActorName(actorName, pageable);
        return ResponseEntity.ok(logs);
    }

    @Operation(
            summary = "Get audit logs by action type",
            description = "Retrieve logs filtered by action type (e.g., CREATE, UPDATE, DELETE)"
    )
    @GetMapping("/action/{actionType}")
    public ResponseEntity<Page<AuditLog>> getLogsByActionType(
            @Parameter(description = "Action type", required = true,
                    schema = @Schema(implementation = ActionType.class))
            @PathVariable ActionType actionType,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditLogService.getLogsByActionType(actionType, pageable);
        return ResponseEntity.ok(logs);
    }

    @Operation(
            summary = "Get audit logs by date range",
            description = "Retrieve audit logs within a specific date range"
    )
    @GetMapping("/date-range")
    public ResponseEntity<Page<AuditLog>> getLogsByDateRange(
            @Parameter(description = "Start date and time", required = true,
                    example = "2024-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date and time", required = true,
                    example = "2024-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditLogService.getLogsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    @Operation(
            summary = "Get audit logs by correlation ID",
            description = "Retrieve related audit logs that share the same correlation ID"
    )
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<AuditLog>> getLogsByCorrelationId(
            @Parameter(description = "Correlation ID", required = true)
            @PathVariable String correlationId) {
        List<AuditLog> logs = auditLogService.getLogsByCorrelationId(correlationId);
        return ResponseEntity.ok(logs);
    }

    @Operation(
            summary = "Get total audit log count",
            description = "Retrieve the total number of audit logs in the system"
    )
    @GetMapping("/stats/total-count")
    public ResponseEntity<Map<String, Long>> getTotalLogCount() {
        long count = auditLogService.getTotalLogCount();
        return ResponseEntity.ok(Map.of("totalCount", count));
    }

    @Operation(
            summary = "Get audit log counts by action type",
            description = "Retrieve count of audit logs grouped by action type"
    )
    @GetMapping("/stats/count-by-action")
    public ResponseEntity<Map<ActionType, Long>> getLogCountsByActionType() {
        Map<ActionType, Long> counts = auditLogService.getLogCountsByActionType();
        return ResponseEntity.ok(counts);
    }

    @Operation(
            summary = "Get audit log counts by entity type",
            description = "Retrieve count of audit logs grouped by entity type"
    )
    @GetMapping("/stats/count-by-entity")
    public ResponseEntity<Map<EntityType, Long>> getLogCountsByEntityType() {
        Map<EntityType, Long> counts = auditLogService.getLogCountsByEntityType();
        return ResponseEntity.ok(counts);
    }

    @Operation(
            summary = "Get audit log count for specific action type",
            description = "Retrieve count of logs for a specific action type"
    )
    @GetMapping("/stats/action/{actionType}/count")
    public ResponseEntity<Map<String, Object>> getLogCountByActionType(
            @Parameter(description = "Action type", required = true)
            @PathVariable ActionType actionType) {
        long count = auditLogService.getLogCountByActionType(actionType);
        return ResponseEntity.ok(Map.of(
                "actionType", actionType.name(),
                "count", count
        ));
    }

    @Operation(
            summary = "Get audit log count for specific entity type",
            description = "Retrieve count of logs for a specific entity type"
    )
    @GetMapping("/stats/entity/{entityType}/count")
    public ResponseEntity<Map<String, Object>> getLogCountByEntityType(
            @Parameter(description = "Entity type", required = true)
            @PathVariable EntityType entityType) {
        long count = auditLogService.getLogCountByEntityType(entityType);
        return ResponseEntity.ok(Map.of(
                "entityType", entityType.name(),
                "count", count
        ));
    }
}
