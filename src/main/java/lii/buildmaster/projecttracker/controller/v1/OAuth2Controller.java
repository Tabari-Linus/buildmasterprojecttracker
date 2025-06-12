package lii.buildmaster.projecttracker.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class OAuth2Controller {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @GetMapping("/providers")
    public Map<String, Object> getOAuth2Providers() {
        Map<String, Object> providers = new HashMap<>();

        Map<String, String> google = new HashMap<>();
        google.put("authUrl", "/oauth2/authorize/google");
        google.put("clientId", googleClientId);
        providers.put("google", google);

        Map<String, String> github = new HashMap<>();
        github.put("authUrl", "/oauth2/authorize/github");
        github.put("clientId", githubClientId);
        providers.put("github", github);

        return providers;
    }
}