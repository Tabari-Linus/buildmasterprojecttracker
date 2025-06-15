package lii.buildmaster.projecttracker;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lii.buildmaster.projecttracker.model.dto.request.LoginRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.RegisterRequestDto;
import lii.buildmaster.projecttracker.model.dto.request.TokenRefreshRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.*;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.security.oauth2.CustomOAuth2User;
import lii.buildmaster.projecttracker.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lii.buildmaster.projecttracker.controller.v1.AuthControllerV1;
import lii.buildmaster.projecttracker.util.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerV1Test {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private DeveloperRepository developerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private AuthControllerV1 authController;

    private User testUser;
    private Role testRole;
    private Developer testDeveloper;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName(RoleName.ROLE_DEVELOPER);

        testUser = User.builder()
                .id(1L).username("testuser").email("developer@gmail.com")
                .password("encodedPassword").firstName("Test").lastName("User")
                .provider(AuthProvider.LOCAL).enabled(true).roles(Set.of(testRole))
                .build();

        testDeveloper = new Developer();
        testDeveloper.setId(1L);
        testDeveloper.setName("Test User");
        testDeveloper.setEmail("developer@gmail.com");
        testDeveloper.setUser(testUser);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void login_Success() {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("developer@gmail.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("refresh-token");
        doNothing().when(userRepository).updateLastLogin(eq(1L), any(LocalDateTime.class));
        var response = authController.authenticateUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponseDto jwtResponse = (JwtResponseDto) response.getBody();
        assertEquals("jwt-token", jwtResponse.getToken());
        assertEquals("developer@gmail.com", jwtResponse.getEmail());
    }

    @Test
    void login_InvalidCredentials() {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("developer@gmail.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authController.authenticateUser(request));
    }

    @Test
    void register_Success() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRole("DEVELOPER");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_DEVELOPER)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(developerRepository.save(any(Developer.class))).thenReturn(testDeveloper);

        var response = authController.registerUser(request);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertTrue(messageResponse.isSuccess());
        assertEquals("User registered successfully! You can now log in.", messageResponse.getMessage());
    }

    @Test
    void register_UsernameExists() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("existinguser");
        request.setEmail("new@test.com");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);


        var response = authController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertFalse(messageResponse.isSuccess());
        assertEquals("Username is already taken!", messageResponse.getMessage());
    }

    @Test
    void getOAuth2Tokens_Success() {

        Cookie jwtCookie = new Cookie(OAuth2AuthenticationSuccessHandler.JWT_COOKIE_NAME, "jwt-token");
        Cookie refreshCookie = new Cookie(OAuth2AuthenticationSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, "refresh-token");

        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{jwtCookie, refreshCookie});
        when(jwtUtils.validateJwtToken("jwt-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("jwt-token")).thenReturn("testuser");
        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(testUser));


        var response = authController.getOAuth2Tokens(httpServletRequest);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponseDto jwtResponse = (JwtResponseDto) response.getBody();
        assertEquals("jwt-token", jwtResponse.getToken());
    }

    @Test
    void getOAuth2Tokens_NoCookies() {

        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{});


        var response = authController.getOAuth2Tokens(httpServletRequest);


        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertEquals("Tokens not found or expired. Please log in again.", messageResponse.getMessage());
    }

    @Test
    void refreshToken_Success() {

        TokenRefreshRequestDto request = new TokenRefreshRequestDto();
        request.setRefreshToken("valid-refresh-token");

        when(jwtUtils.validateJwtToken("valid-refresh-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid-refresh-token")).thenReturn("testuser");
        when(jwtUtils.generateTokenFromUsername("testuser")).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("new-refresh-token");


        var response = authController.refreshToken(request);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        TokenRefreshResponseDto tokenResponse = (TokenRefreshResponseDto) response.getBody();
        assertEquals("new-access-token", tokenResponse.getAccessToken());
    }

    @Test
    void refreshToken_Invalid() {

        TokenRefreshRequestDto request = new TokenRefreshRequestDto();
        request.setRefreshToken("invalid-token");

        when(jwtUtils.validateJwtToken("invalid-token")).thenReturn(false);


        var response = authController.refreshToken(request);


        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertEquals("Refresh token is invalid or expired!", messageResponse.getMessage());
    }

    @Test
    void logout_Success() {

        var response = authController.logoutUser();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertEquals("Logged out successfully!", messageResponse.getMessage());
    }

    @Test
    void validateToken_Valid() {

        String authHeader = "Bearer valid-token";
        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid-token")).thenReturn("testuser");


        var response = authController.validateToken(authHeader);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertEquals("Token is valid for user: testuser", messageResponse.getMessage());
    }

    @Test
    void validateToken_Invalid() {

        String authHeader = "Bearer invalid-token";
        when(jwtUtils.validateJwtToken("invalid-token")).thenReturn(false);


        var response = authController.validateToken(authHeader);


        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertEquals("Invalid or expired token", messageResponse.getMessage());
    }

    @Test
    void getMyDetails_AuthenticatedUser() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(developerRepository.findDeveloperByEmail("test@example.com")).thenReturn(testDeveloper);


        var response = authController.getMyDetails();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthenticatedUserResponseDto userResponse = (AuthenticatedUserResponseDto) response.getBody();
        assertEquals("testuser", userResponse.getUsername());
        assertNotNull(userResponse.getDeveloper());
    }

    @Test
    void getMyDetails_OAuth2User() {

        CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);


        var response = authController.getMyDetails();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(oAuth2User, response.getBody());
    }

    @Test
    void getMyDetails_NotAuthenticated() {

        when(securityContext.getAuthentication()).thenReturn(null);


        var response = authController.getMyDetails();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        MessageResponseDto messageResponse = (MessageResponseDto) response.getBody();
        assertEquals("User is not authenticated", messageResponse.getMessage());
    }
}