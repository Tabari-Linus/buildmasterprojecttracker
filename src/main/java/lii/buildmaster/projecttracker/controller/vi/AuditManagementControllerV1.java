package lii.buildmaster.projecttracker.controller.vi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lii.buildmaster.projecttracker.util.AuditCleanupUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit-management")
@Tag(name = "Audit Management", description = "Administrative operations for audit log management")
public class AuditManagementControllerV1 {

    private final AuditCleanupUtil auditCleanupUtil;

    public AuditManagementControllerV1(AuditCleanupUtil auditCleanupUtil) {
        this.auditCleanupUtil = auditCleanupUtil;
    }

    @Operation(
            summary = "Get audit log count",
            description = "Retrieve the current number of audit logs in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getAuditLogCount() {
        long count = auditCleanupUtil.getAuditLogCount();
        return ResponseEntity.ok(Map.of("auditLogCount", count));
    }

    @Operation(
            summary = "Clear all audit logs",
            description = "WARNING: This will permanently delete ALL audit logs. Use only for development/testing!"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs cleared successfully")
    })
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, String>> clearAllAuditLogs() {
        long countBefore = auditCleanupUtil.getAuditLogCount();
        auditCleanupUtil.clearAllAuditLogs();

        return ResponseEntity.ok(Map.of(
                "message", "All audit logs cleared successfully",
                "clearedCount", String.valueOf(countBefore)
        ));
    }
}
