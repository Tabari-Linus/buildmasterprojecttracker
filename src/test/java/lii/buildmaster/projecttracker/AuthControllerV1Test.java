package lii.buildmaster.projecttracker;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lii.buildmaster.projecttracker.controller.v1.AuthControllerV1;
import lii.buildmaster.projecttracker.model.dto.request.*;
import lii.buildmaster.projecttracker.model.dto.response.*;
import lii.buildmaster.projecttracker.model.entity.*;
import lii.buildmaster.projecttracker.model.enums.*;
import lii.buildmaster.projecttracker.repository.jpa.*;
import lii.buildmaster.projecttracker.security.oauth2.CustomOAuth2User;
import lii.buildmaster.projecttracker.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lii.buildmaster.projecttracker.util.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerV1Test {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private DeveloperRepository developerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private HttpServletRequest request;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private AuthControllerV1 controller;

    private User mockUser;
    private Role devRole;
    private Developer mockDev;

    @BeforeEach
    void init() {
        devRole = new Role(1L, RoleName.ROLE_DEVELOPER, new HashSet<>());
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("developer@gmail.com")
                .firstName("Test")
                .lastName("User")
                .password("encoded")
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .roles(Set.of(devRole))
                .build();

        mockDev = new Developer(1L, "Test User",  mockUser,"developer@gmail.com");

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void authenticateUser_ShouldReturnTokens_WhenCredentialsValid() {
        LoginRequestDto login = new LoginRequestDto("developer@gmail.com", "password123");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("refresh-token");
        doNothing().when(userRepository).updateLastLogin(eq(1L), any(LocalDateTime.class));

        var response = controller.login(login);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JwtResponseDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals("jwt-token", dto.getToken());
        assertEquals("refresh-token", dto.getRefreshToken());
        assertEquals("developer@gmail.com", dto.getEmail());
    }

    @Test
    void authenticateUser_ShouldThrow_WhenBadCredentials() {
        LoginRequestDto login = new LoginRequestDto("developer@gmail.com", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

        assertThrows(BadCredentialsException.class, () -> controller.login(login));
    }

    @Test
    void registerUser_ShouldReturnSuccess_WhenDataIsValid() {
        RegisterRequestDto register = new RegisterRequestDto("newuser", "new@test.com", "pass123", "New", "User", "DEVELOPER");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(roleRepository.findByName(RoleName.ROLE_DEVELOPER)).thenReturn(Optional.of(devRole));
        when(userRepository.save(any())).thenReturn(mockUser);
        when(developerRepository.save(any())).thenReturn(mockDev);

        var response = controller.register(register);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((MessageResponseDto) response.getBody()).isSuccess());
    }

    @Test
    void registerUser_ShouldFail_WhenUsernameExists() {
        RegisterRequestDto register = new RegisterRequestDto("newuser", "new@test.com", "pass123", "New", "User", "DEVELOPER");

        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        var response = controller.register(register);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(((MessageResponseDto) response.getBody()).isSuccess());
    }

    @Test
    void getOAuth2Tokens_ShouldReturnJwt_WhenCookiesPresent() {
        Cookie jwt = new Cookie(OAuth2AuthenticationSuccessHandler.JWT_COOKIE_NAME, "jwt-token");
        Cookie refresh = new Cookie(OAuth2AuthenticationSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, "refresh-token");

        when(request.getCookies()).thenReturn(new Cookie[]{jwt, refresh});
        when(jwtUtils.validateJwtToken("jwt-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("jwt-token")).thenReturn("testuser");
        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(mockUser));

        var response = controller.getOAuth2Token(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", ((JwtResponseDto) response.getBody()).getToken());
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenRefreshTokenValid() {
        TokenRefreshRequestDto refreshRequest = new TokenRefreshRequestDto("valid-refresh-token");

        when(jwtUtils.validateJwtToken("valid-refresh-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid-refresh-token")).thenReturn("testuser");
        when(jwtUtils.generateTokenFromUsername("testuser")).thenReturn("new-jwt");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("new-refresh");

        var response = controller.refresh(refreshRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-jwt", ((TokenRefreshResponseDto) response.getBody()).getAccessToken());
    }

    @Test
    void getMyDetails_ShouldReturnUserInfo_WhenUserIsAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(developerRepository.findDeveloperByEmail("developer@gmail.com")).thenReturn(mockDev);

        var response = controller.getUserDetails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", ((AuthenticatedUserResponseDto) response.getBody()).getUsername());
    }

    @Test
    void getMyDetails_ShouldReturnOAuth2Principal_WhenOAuth2User() {
        CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        var response = controller.getUserDetails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(oAuth2User, response.getBody());
    }

    @Test
    void validateToken_ShouldReturnSuccess_WhenTokenValid() {
        when(jwtUtils.validateJwtToken("token123")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("token123")).thenReturn("testuser");

        var response = controller.validate("Bearer token123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((MessageResponseDto) response.getBody()).getMessage().contains("testuser"));
    }

    @Test
    void logoutUser_ShouldReturnSuccess() {
        var response = controller.logout();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Logged out successfully!", ((MessageResponseDto) response.getBody()).getMessage());
    }
}
