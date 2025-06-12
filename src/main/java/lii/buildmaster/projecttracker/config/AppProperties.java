package lii.buildmaster.projecttracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final OAuth2 oauth2 = new OAuth2();
    private final Jwt jwt = new Jwt();

    public static class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();
        private String defaultSuccessUrl;
        private String defaultFailureUrl;

        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }

        public void setAuthorizedRedirectUris(List<String> authorizedRedirectUris) {
            this.authorizedRedirectUris = authorizedRedirectUris;
        }

        public String getDefaultSuccessUrl() {
            return defaultSuccessUrl;
        }

        public void setDefaultSuccessUrl(String defaultSuccessUrl) {
            this.defaultSuccessUrl = defaultSuccessUrl;
        }

        public String getDefaultFailureUrl() {
            return defaultFailureUrl;
        }

        public void setDefaultFailureUrl(String defaultFailureUrl) {
            this.defaultFailureUrl = defaultFailureUrl;
        }
    }

    public static class Jwt {
        private String secret;
        private int expirationMs;
        private int refreshExpirationMs;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getExpirationMs() {
            return expirationMs;
        }

        public void setExpirationMs(int expirationMs) {
            this.expirationMs = expirationMs;
        }

        public int getRefreshExpirationMs() {
            return refreshExpirationMs;
        }

        public void setRefreshExpirationMs(int refreshExpirationMs) {
            this.refreshExpirationMs = refreshExpirationMs;
        }
    }

    public OAuth2 getOauth2() {
        return oauth2;
    }

    public Jwt getJwt() {
        return jwt;
    }
}