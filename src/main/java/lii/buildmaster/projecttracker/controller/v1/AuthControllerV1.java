package lii.buildmaster.projecttracker.controller.v1;

import jakarta.servlet.http.HttpServletRequest;
import lii.buildmaster.projecttracker.model.dto.request.LoginRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.RegisterRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.TokenRefreshRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.*;
import lii.buildmaster.projecttracker.service.AuthService;
import lii.buildmaster.projecttracker.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        return ResponseEntity.ok(authService.authenticate(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refresh(@Valid @RequestBody TokenRefreshRequestDto request) {
        return ResponseEntity.ok(tokenService.refreshToken(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<MessageResponseDto> validate(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(tokenService.validate(token));
    }

    @GetMapping("/my-details")
    public ResponseEntity<AuthenticatedUserResponseDto> getUserDetails() {
        return ResponseEntity.ok(authService.getAuthenticatedUserDetails());
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout() {
        authService.logout();
        return ResponseEntity.ok(MessageResponseDto.success("Logged out successfully!"));
    }

    @GetMapping("/oauth2-token")
    public ResponseEntity<JwtResponseDto> getOAuth2Token(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getOAuth2TokensFromCookies(request));
    }
}

