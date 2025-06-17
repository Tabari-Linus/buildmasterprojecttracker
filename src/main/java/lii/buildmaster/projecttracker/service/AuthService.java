package lii.buildmaster.projecttracker.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lii.buildmaster.projecttracker.model.dto.request.LoginRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.RegisterRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.AuthenticatedUserResponseDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.dto.response.JwtResponseDto;
import lii.buildmaster.projecttracker.model.dto.response.MessageResponseDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.security.oauth2.CookieUtils;
import lii.buildmaster.projecttracker.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lii.buildmaster.projecttracker.util.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;

    public JwtResponseDto authenticate(LoginRequestDto loginRequest) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        return new JwtResponseDto(jwt, refreshToken, user.getId(), user.getUsername(), user.getEmail(),
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    public MessageResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        User user = buildUser(request);
        Role role = resolveRole(request.getRole());

        user.setRoles(Set.of(role));
        User savedUser = userRepository.save(user);

        if (role.getName() == RoleName.ROLE_DEVELOPER) {
            Developer dev = new Developer(savedUser.getFullName(), savedUser.getEmail(), savedUser, "");
            developerRepository.save(dev);
        }

        return MessageResponseDto.success("User registered successfully!");
    }

    private User buildUser(RegisterRequestDto request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .provider(AuthProvider.LOCAL)
                .build();
    }

    private Role resolveRole(String roleStr) {
        final RoleName parsedRoleName;
        RoleName parsedRoleName1;
        try {
            parsedRoleName1 = RoleName.valueOf("ROLE_" + roleStr.toUpperCase());
        } catch (Exception ignored) {
            parsedRoleName1 = RoleName.ROLE_DEVELOPER;
        }
        parsedRoleName = parsedRoleName1;
        return roleRepository.findByName(parsedRoleName)
                .orElseThrow(() -> new RuntimeException("Role " + parsedRoleName + " not found."));
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    public AuthenticatedUserResponseDto getAuthenticatedUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        if (auth.getPrincipal() instanceof User user) {
            Developer dev = developerRepository.findDeveloperByEmail(user.getEmail());
            DeveloperResponseDto devDto = (dev != null) ? new DeveloperResponseDto(
                    dev.getId(), dev.getName(), dev.getEmail(), dev.getSkills()) : null;

            return new AuthenticatedUserResponseDto(user.getId(), user.getUsername(), user.getEmail(), devDto);
        }
        throw new RuntimeException("Unexpected user principal type");
    }

    public JwtResponseDto getOAuth2TokensFromCookies(HttpServletRequest request) {
        String jwt = CookieUtils.getCookie(request, OAuth2AuthenticationSuccessHandler.JWT_COOKIE_NAME)
                .map(Cookie::getValue).orElseThrow();
        String refresh = CookieUtils.getCookie(request, OAuth2AuthenticationSuccessHandler.REFRESH_TOKEN_COOKIE_NAME)
                .map(Cookie::getValue).orElseThrow();

        if (!jwtUtils.validateJwtToken(jwt)) {
            throw new RuntimeException("Invalid JWT token");
        }

        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow();

        return new JwtResponseDto(jwt, refresh, user.getId(), user.getUsername(), user.getEmail(),
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}