package lii.buildmaster.projecttracker.util.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.security.oauth2.CustomOAuth2User;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Getter
    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    @Getter
    @Value("${app.jwt.refresh-expiration-ms}")
    private int refreshTokenExpirationMs;

    private Key key() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

        if (keyBytes.length < 64) {
            logger.warn("JWT secret key is too short for HS512. Generating a secure key.");

            SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            return key;
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(Authentication authentication) {
        String username;
        List<String> roles;

        if (authentication.getPrincipal() instanceof User userPrincipal) {
            username = userPrincipal.getUsername();
            roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else if (authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            username = oAuth2User.getUsername();

            roles = oAuth2User.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Unknown principal type: " +
                    authentication.getPrincipal().getClass().getName());
        }

        return generateToken(username, roles);
    }

    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return createToken(claims, username, jwtExpirationMs);
    }

    public String generateTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, jwtExpirationMs);
    }


    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, refreshTokenExpirationMs);
    }

    private String createToken(Map<String, Object> claims, String subject, int expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationTime))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(key())
                    .build()
                    .parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }
}
