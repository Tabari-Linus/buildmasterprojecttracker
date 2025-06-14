package lii.buildmaster.projecttracker.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
