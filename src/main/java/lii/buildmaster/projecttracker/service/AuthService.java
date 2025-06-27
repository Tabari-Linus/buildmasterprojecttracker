package lii.buildmaster.projecttracker.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lii.buildmaster.projecttracker.mapper.DeveloperMapper;
import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
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
import org.springframework.transaction.annotation.Transactional; // Added for explicit transaction management if needed

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeveloperService developerService;
    private final PasswordEncoder passwordEncoder;
    private final DeveloperMapper developerMapper;

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
            if (request.getSkills() == null || request.getSkills().isEmpty()) {
                throw new IllegalArgumentException("Skills are required for developers!");
            }

            DeveloperRequestDto developerDto = new DeveloperRequestDto(
                    request.getFirstName() + " " + request.getLastName(),
                    request.getEmail(),
                    request.getSkills(),
                    request.getPassword()
            );
            developerService.createDeveloper(developerDto, savedUser);
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

    @Transactional(readOnly = true) // Added transactional context to ensure lazy loaded developer is available if needed
    public AuthenticatedUserResponseDto getAuthenticatedUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            // Re-fetch the user from the repository with developer eagerly loaded.
            // This avoids LazyInitializationException if the 'user' object from principal is detached
            // or doesn't have the developer eagerly loaded (Spring Security's principal might not).
            User fetchedUser = userRepository.findByUsernameWithDeveloper(user.getUsername()) // This method now has @Query
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            DeveloperResponseDto devDto = new DeveloperResponseDto();

            // Use fetchedUser to check roles and developer
            boolean isDeveloper = fetchedUser.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleName.ROLE_DEVELOPER);

            if (isDeveloper) {
                // If developer is already fetched via EntityGraph, it's accessible directly
                // If not, developerService.getDeveloperByEmail will perform another query.
                // The findByUsernameWithDeveloper above should fetch it directly.
                Developer dev = fetchedUser.getDeveloper();
                System.out.println("Developer Entity: " + dev); // Good for debugging
                if (dev == null) {
                    throw new RuntimeException("Developer not found for user: " + fetchedUser.getEmail());
                }

                devDto = developerMapper.toResponseDto(dev);
            }

            return new AuthenticatedUserResponseDto(
                    fetchedUser.getId(), fetchedUser.getUsername(), fetchedUser.getEmail(), devDto
            );
        }
        throw new RuntimeException("Unexpected user principal type");
    }

    @Transactional(readOnly = true) // Added transactional context
    public JwtResponseDto getOAuth2TokensFromCookies(HttpServletRequest request) {
        String jwt = CookieUtils.getCookie(request, OAuth2AuthenticationSuccessHandler.JWT_COOKIE_NAME)
                .map(Cookie::getValue).orElseThrow();
        String refresh = CookieUtils.getCookie(request, OAuth2AuthenticationSuccessHandler.REFRESH_TOKEN_COOKIE_NAME)
                .map(Cookie::getValue).orElseThrow();

        if (!jwtUtils.validateJwtToken(jwt)) {
            throw new RuntimeException("Invalid JWT token");
        }

        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        // Use the new EntityGraph-enabled method to fetch user with developer
        User user = userRepository.findUserWithDeveloperByUsernameOrEmail(username, username) // Renamed method and has @Query
                .orElseThrow(() -> new RuntimeException("User not found for token validation"));

        return new JwtResponseDto(jwt, refresh, user.getId(), user.getUsername(), user.getEmail(),
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}