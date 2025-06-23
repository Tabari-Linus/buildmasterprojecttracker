package lii.buildmaster.projecttracker.controller.v1;

import lii.buildmaster.projecttracker.model.dto.info.OAuth2ProviderInfo;
import lii.buildmaster.projecttracker.model.dto.response.OAuth2ProvidersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @GetMapping("/providers")
    public OAuth2ProvidersResponse getOAuth2Providers() {
        Map<String, OAuth2ProviderInfo> providers = new HashMap<>();

        providers.put("google", new OAuth2ProviderInfo("/oauth2/authorize/google", googleClientId));
        providers.put("github", new OAuth2ProviderInfo("/oauth2/authorize/github", githubClientId));

        return new OAuth2ProvidersResponse(providers);
    }

}