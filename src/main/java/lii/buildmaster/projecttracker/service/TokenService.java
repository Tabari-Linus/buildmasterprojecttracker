package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.dto.request.TokenRefreshRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.MessageResponseDto;
import lii.buildmaster.projecttracker.model.dto.response.TokenRefreshResponseDto;
import lii.buildmaster.projecttracker.util.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtils;

    public TokenRefreshResponseDto refreshToken(TokenRefreshRequestDto request) {
        if (!jwtUtils.validateJwtToken(request.getRefreshToken())) {
            throw new RuntimeException("Refresh token is invalid");
        }
        String username = jwtUtils.getUserNameFromJwtToken(request.getRefreshToken());
        return new TokenRefreshResponseDto(
                jwtUtils.generateTokenFromUsername(username),
                jwtUtils.generateRefreshToken(username),
                "Bearer"
        );
    }

    public MessageResponseDto validate(String token) {
        String jwt = token.replace("Bearer ", "");
        if (!jwtUtils.validateJwtToken(jwt)) {
            throw new RuntimeException("Invalid token");
        }
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        return MessageResponseDto.success("Token is valid for user: " + username);
    }
}

