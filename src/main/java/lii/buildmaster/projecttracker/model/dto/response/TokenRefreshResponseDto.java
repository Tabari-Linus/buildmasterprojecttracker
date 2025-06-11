package lii.buildmaster.projecttracker.model.dto.response;

public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
