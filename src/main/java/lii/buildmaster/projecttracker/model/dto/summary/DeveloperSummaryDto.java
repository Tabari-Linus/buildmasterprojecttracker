package lii.buildmaster.projecttracker.model.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperSummaryDto {

    private Long id;
    private String name;
    private String email;
    private long activeTaskCount;
}
