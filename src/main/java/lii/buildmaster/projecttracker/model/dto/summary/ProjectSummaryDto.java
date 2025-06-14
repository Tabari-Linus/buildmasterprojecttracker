package lii.buildmaster.projecttracker.model.dto.summary;

import com.fasterxml.jackson.annotation.JsonFormat;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSummaryDto {

    private Long id;
    private String name;
    private ProjectStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;

    private long taskCount;
    private long completedTaskCount;
}
