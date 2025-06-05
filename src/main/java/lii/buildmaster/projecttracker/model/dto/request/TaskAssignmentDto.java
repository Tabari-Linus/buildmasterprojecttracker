package lii.buildmaster.projecttracker.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentDto {

    @NotNull(message = "Developer ID is required")
    private Long developerId;
}
