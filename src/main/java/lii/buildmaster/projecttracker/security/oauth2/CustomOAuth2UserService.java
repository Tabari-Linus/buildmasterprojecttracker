package lii.buildmaster.projecttracker.security.oauth2;


import lii.buildmaster.projecttracker.config.SecurityConfig;
import lii.buildmaster.projecttracker.exception.OAuth2AuthenticationProcessingException;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes()
        );

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            // Check if user registered with different provider
            if (!user.getProvider().equals(AuthProvider.valueOf(
                    oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()))) {
                throw new OAuth2AuthenticationProcessingException(
                        "Looks like you're signed up with " + user.getProvider() + " account. " +
                                "Please use your " + user.getProvider() + " account to login."
                );
            }

            // Update existing user
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            // Register new user
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return CustomOAuth2User.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        user.setProvider(AuthProvider.valueOf(
                oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setEmail(oAuth2UserInfo.getEmail());

        // Generate username from email
        String username = generateUniqueUsername(oAuth2UserInfo.getEmail());
        user.setUsername(username);

        // Set name
        String name = oAuth2UserInfo.getName();
        if (StringUtils.hasText(name)) {
            String[] nameParts = name.split(" ", 2);
            user.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                user.setLastName(nameParts[1]);
            }
        }

        // OAuth users don't have passwords
        user.setPassword(new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()));
        user.setEnabled(true);

        // Assign default CONTRACTOR role for OAuth users
        Role contractorRole = roleRepository.findByName(RoleName.ROLE_CONTRACTOR)
                .orElseThrow(() -> new OAuth2AuthenticationProcessingException("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(contractorRole);
        user.setRoles(roles);

        log.info("Registering new OAuth2 user: {} with provider: {}",
                user.getEmail(), user.getProvider());

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        // Update user info if needed
        if (!StringUtils.hasText(existingUser.getFirstName()) && StringUtils.hasText(oAuth2UserInfo.getName())) {
            String[] nameParts = oAuth2UserInfo.getName().split(" ", 2);
            existingUser.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                existingUser.setLastName(nameParts[1]);
            }
        }

        existingUser.setProviderId(oAuth2UserInfo.getId());

        return userRepository.save(existingUser);
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase();
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}
