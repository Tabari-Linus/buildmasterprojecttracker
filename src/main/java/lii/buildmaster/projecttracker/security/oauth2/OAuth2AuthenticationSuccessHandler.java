package lii.buildmaster.projecttracker.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lii.buildmaster.projecttracker.exception.BadRequestException;
import lii.buildmaster.projecttracker.model.dto.response.JwtResponseDto;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.security.oauth2.CookieUtils;
import lii.buildmaster.projecttracker.util.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    public static final String JWT_COOKIE_NAME = "jwt";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Value("${app.oauth2.default-success-url}")
    private String defaultSuccessUrl;

    @Value("${app.oauth2.authorized-redirect-uris}")
    private String[] authorizedRedirectUris;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Optional<String> redirectUri = CookieUtils.getCookie(request,
                        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }
        String frontendCallbackUrl = redirectUri.orElse(defaultSuccessUrl);

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication.getName());

        Long userId = null;
        String username = null;
        String email = null;
        List<String> roles = null;

        if (authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            userId = oAuth2User.getId();
            username = oAuth2User.getUsername();
            email = oAuth2User.getEmail();
            roles = oAuth2User.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            log.info("OAuth2 authentication successful for user: {} (ID: {})", email, userId);
        } else if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            userId = user.getId();
            username = user.getUsername();
            email = user.getEmail();
            roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            log.info("Local user authentication successful: {} (ID: {})", email, userId);
        }

        updateLastLogin(authentication);

        clearAuthenticationAttributes(request, response);

        JwtResponseDto jwtResponse = new JwtResponseDto(jwt, refreshToken, userId, username, email, roles);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(jwtResponse));
        response.getWriter().flush();

    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        return defaultSuccessUrl;
    }


    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        for (String authorizedRedirectUri : authorizedRedirectUris) {
            URI authorizedURI = URI.create(authorizedRedirectUri);
            if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                    && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                return true;
            }
        }
        return false;
    }

    private void updateLastLogin(Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
                Optional<User> userOptional = userRepository.findById(oAuth2User.getId());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(user);
                    log.debug("Updated last login time for user: {}", user.getEmail());
                }
            } else if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                Optional<User> userOptional = userRepository.findById(user.getId());
                if (userOptional.isPresent()) {
                    User localUser = userOptional.get();
                    localUser.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(localUser);
                    log.debug("Updated last login time for local user: {}", localUser.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("Failed to update last login time", e);
        }
    }
}
