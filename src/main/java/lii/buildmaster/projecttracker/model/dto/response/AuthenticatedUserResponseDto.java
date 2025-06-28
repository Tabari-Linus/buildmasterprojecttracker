package lii.buildmaster.projecttracker.model.dto.response;

import lii.buildmaster.projecttracker.model.dto.summary.DeveloperSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserResponseDto {
    private Long id;
    private String username;
    private String  email;
    private DeveloperSummaryDto developer;
}