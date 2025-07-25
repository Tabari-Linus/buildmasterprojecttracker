package lii.buildmaster.projecttracker.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lii.buildmaster.projecttracker.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateRequestDto {
    @NotEmpty(message = "At least one role must be specified")
    private Set<Role> roles;

}
