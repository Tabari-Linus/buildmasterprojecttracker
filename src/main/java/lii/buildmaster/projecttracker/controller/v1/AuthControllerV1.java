package lii.buildmaster.projecttracker.controller.v1;

import lii.buildmaster.projecttracker.model.dto.request.LoginRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.RegisterRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.TokenRefreshRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.JwtResponseDto;
import lii.buildmaster.projecttracker.model.dto.response.MessageResponseDto;
import lii.buildmaster.projecttracker.model.dto.response.TokenRefreshResponseDto;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.util.jwt.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerV1.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(
                    ((User) authentication.getPrincipal()).getUsername());

            User userDetails = (User) authentication.getPrincipal();

            userRepository.updateLastLogin(userDetails.getId(), LocalDateTime.now());

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            logger.info("User {} logged in successfully", userDetails.getEmail());

            return ResponseEntity.ok(new JwtResponseDto(
                    jwt,
                    refreshToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto registerRequest) {
        logger.info("Registration attempt for user: {} with email: {}",
                registerRequest.getUsername(), registerRequest.getEmail());

        try {
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                logger.warn("Registration failed: Username {} already exists", registerRequest.getUsername());
                return ResponseEntity.badRequest()
                        .body(MessageResponseDto.error("Username is already taken!"));
            }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
                logger.warn("Registration failed: Email {} already exists", registerRequest.getEmail());
                return ResponseEntity.badRequest()
                        .body(MessageResponseDto.error("Email is already in use!"));
            }

            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .provider(AuthProvider.LOCAL)
                    .enabled(true)
                    .build();

            Set<Role> roles = new HashSet<>();
            String requestedRole = registerRequest.getRole();

            if (requestedRole == null || requestedRole.isEmpty()) {
                requestedRole = "DEVELOPER";
            }

            RoleName roleName;
            try {
                roleName = RoleName.valueOf("ROLE_" + requestedRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role requested: {}, defaulting to DEVELOPER", requestedRole);
                roleName = RoleName.ROLE_DEVELOPER;
            }

            logger.info("Looking for role: {}", roleName);

            RoleName finalRoleName = roleName;
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        logger.error("Role {} not found in database", finalRoleName);
                        return new RuntimeException("Role " + finalRoleName + " is not found. Please ensure database is initialized.");
                    });

            logger.info("Found role: {} with id: {}", role.getName(), role.getId());

            roles.add(role);
            user.setRoles(roles);

            User savedUser = userRepository.save(user);
            logger.info("User {} saved successfully with ID: {}", savedUser.getUsername(), savedUser.getId());

            if (roleName == RoleName.ROLE_DEVELOPER) {
                try {
                    Developer developer = new Developer();
                    developer.setName(savedUser.getFirstName() + " " + savedUser.getLastName());
                    developer.setEmail(savedUser.getEmail());
                    developer.setUser(savedUser);
                    developer.setSkills("");

                    developerRepository.save(developer);
                    logger.info("Created developer entity for user: {}", savedUser.getUsername());
                } catch (Exception e) {
                    logger.error("Failed to create developer entity for user: {}", savedUser.getUsername(), e);
                }
            }

            logger.info("User {} registered successfully with role: {}",
                    savedUser.getEmail(), roleName);

            return ResponseEntity.ok(MessageResponseDto.success(
                    "User registered successfully! You can now log in."));

        } catch (Exception e) {
            logger.error("Registration failed for user: {}", registerRequest.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequestDto request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            if (jwtUtils.validateJwtToken(requestRefreshToken)) {
                String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
                String newAccessToken = jwtUtils.generateTokenFromUsername(username);
                String newRefreshToken = jwtUtils.generateRefreshToken(username);

                return ResponseEntity.ok(new TokenRefreshResponseDto(
                        newAccessToken,
                        newRefreshToken,
                        "Bearer"));
            }
        } catch (Exception e) {
            logger.error("Refresh token validation failed", e);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponseDto.error("Refresh token is invalid or expired!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(MessageResponseDto.success("Logged out successfully!"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                return ResponseEntity.ok(MessageResponseDto.success("Token is valid for user: " + username));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponseDto.error("Invalid or expired token"));
    }
}