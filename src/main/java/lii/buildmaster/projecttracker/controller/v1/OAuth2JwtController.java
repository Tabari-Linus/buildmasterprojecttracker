package lii.buildmaster.projecttracker.controller.v1;

import lii.buildmaster.projecttracker.model.dto.response.JwtResponseDto;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.util.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class OAuth2JwtController {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @GetMapping("/token")
    public ResponseEntity<?> getJwtToken(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No OAuth2 authentication found");
        }

        try {
            String email = principal.getAttribute("email");
            if (email == null) {
                return ResponseEntity.badRequest()
                        .body("Email not found in OAuth2 profile");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());

            log.info("JWT token generated for OAuth2 user: {}", user.getEmail());

            return ResponseEntity.ok(new JwtResponseDto(
                    jwt,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    roles
            ));

        } catch (Exception e) {
            log.error("Error generating JWT token for OAuth2 user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating JWT token: " + e.getMessage());
        }
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convertOAuth2ToJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No authentication found");
        }

        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            return ResponseEntity.badRequest()
                    .body("Not an OAuth2 authentication");
        }

        return getJwtToken(oAuth2User);
    }
}