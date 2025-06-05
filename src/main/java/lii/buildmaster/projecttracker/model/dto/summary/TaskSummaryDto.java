package lii.buildmaster.projecttracker.model.dto.summary;

import com.fasterxml.jackson.annotation.JsonFormat;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDto {

    private Long id;
    private String title;
    private TaskStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    private String projectName;
    private String developerName;
    private boolean overdue;
}