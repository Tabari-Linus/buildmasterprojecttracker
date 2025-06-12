package lii.buildmaster.projecttracker.controller.v2;

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate using email as username
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(
                    ((User) authentication.getPrincipal()).getUsername());

            User userDetails = (User) authentication.getPrincipal();

            // Update last login time
            userRepository.updateLastLogin(userDetails.getId(), LocalDateTime.now());

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("User {} logged in successfully", userDetails.getEmail());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    refreshToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if username exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("Username is already taken!"));
            }

            // Check if email exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("Email is already in use!"));
            }

            // Create new user
            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .provider(User.AuthProvider.LOCAL)
                    .enabled(true)
                    .build();

            // Assign role
            Set<Role> roles = new HashSet<>();
            String requestedRole = registerRequest.getRole();

            if (requestedRole == null || requestedRole.isEmpty()) {
                // Default role is DEVELOPER
                Role userRole = roleRepository.findByName(Role.RoleName.ROLE_DEVELOPER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                // Validate and assign requested role
                switch (requestedRole.toUpperCase()) {
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "MANAGER":
                        Role managerRole = roleRepository.findByName(Role.RoleName.ROLE_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(managerRole);
                        break;
                    case "DEVELOPER":
                        Role devRole = roleRepository.findByName(Role.RoleName.ROLE_DEVELOPER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(devRole);
                        break;
                    case "CONTRACTOR":
                        Role contractorRole = roleRepository.findByName(Role.RoleName.ROLE_CONTRACTOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(contractorRole);
                        break;
                    default:
                        Role defaultRole = roleRepository.findByName(Role.RoleName.ROLE_DEVELOPER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(defaultRole);
                }
            }

            user.setRoles(roles);
            User savedUser = userRepository.save(user);

            // If role is DEVELOPER, create a Developer entity
            if (roles.stream().anyMatch(role -> role.getName() == Role.RoleName.ROLE_DEVELOPER)) {
                Developer developer = new Developer();
                developer.setName(savedUser.getFirstName() + " " + savedUser.getLastName());
                developer.setEmail(savedUser.getEmail());
                developer.setUser(savedUser);
                developer.setSkills(""); // Default empty skills
                developerRepository.save(developer);
            }

            logger.info("User {} registered successfully with role(s): {}",
                    savedUser.getEmail(),
                    roles.stream().map(r -> r.getName()).collect(Collectors.toList()));

            return ResponseEntity.ok(MessageResponse.success(
                    "User registered successfully! You can now log in."));

        } catch (Exception e) {
            logger.error("Registration failed for user: {}", registerRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            // Validate refresh token
            if (jwtUtils.validateJwtToken(requestRefreshToken)) {
                String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
                String newAccessToken = jwtUtils.generateTokenFromUsername(username);
                String newRefreshToken = jwtUtils.generateRefreshToken(username);

                return ResponseEntity.ok(new TokenRefreshResponse(
                        newAccessToken,
                        newRefreshToken,
                        "Bearer"));
            }
        } catch (Exception e) {
            logger.error("Refresh token validation failed", e);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.error("Refresh token is invalid or expired!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // In a stateless JWT implementation, logout is handled client-side
        // by removing the token. Here we just acknowledge the logout.
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(MessageResponse.success("Logged out successfully!"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                return ResponseEntity.ok(MessageResponse.success("Token is valid for user: " + username));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.error("Invalid or expired token"));
    }
}
