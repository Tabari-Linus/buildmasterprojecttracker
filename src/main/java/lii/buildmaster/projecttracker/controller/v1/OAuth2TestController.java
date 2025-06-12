package lii.buildmaster.projecttracker.controller.v1;

import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2TestController {

    private final UserRepository userRepository;

    @GetMapping("/user")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> userInfo = new HashMap<>();

        if (principal != null) {
            userInfo.put("name", principal.getName());
            userInfo.put("attributes", principal.getAttributes());
            userInfo.put("authorities", principal.getAuthorities());

            // Try to get the user from database
            String email = principal.getAttribute("email");
            if (email != null) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    userInfo.put("userId", user.getId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("roles", user.getRoles());
                });
            }
        }

        return userInfo;
    }

    @GetMapping("/success")
    public Map<String, Object> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OAuth2 login successful!");
        response.put("email", principal.getAttribute("email"));
        response.put("name", principal.getAttribute("name"));

        log.info("OAuth2 login successful for user: {}" + principal.getAttribute("email"));

        return response;
    }
}