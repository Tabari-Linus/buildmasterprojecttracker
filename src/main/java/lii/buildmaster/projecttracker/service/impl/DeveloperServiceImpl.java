package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.exception.DeveloperNotFoundException;
import lii.buildmaster.projecttracker.exception.EmailAlreadyExistsException;
import lii.buildmaster.projecttracker.exception.ProjectNotFoundException;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.AuthProvider;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.model.enums.RoleName;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.repository.jpa.RoleRepository;
import lii.buildmaster.projecttracker.repository.jpa.UserRepository;
import lii.buildmaster.projecttracker.service.AuditLogService;
import lii.buildmaster.projecttracker.service.DeveloperService;
import lii.buildmaster.projecttracker.util.AuditUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int GENERATED_PASSWORD_LENGTH = 12;
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[GENERATED_PASSWORD_LENGTH];
        random.nextBytes(bytes);
        String base64Password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String cleanPassword = base64Password.replaceAll("[^a-zA-Z0-9]", "");
        return cleanPassword.substring(0, Math.min(GENERATED_PASSWORD_LENGTH, cleanPassword.length()));
    }

    @Override
    @Auditable(action = ActionType.CREATE, entityType = EntityType.DEVELOPER)
    @Caching(evict = {
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "developerStats", allEntries = true)
    })
    public Developer createDeveloper(String name, String email, String skills) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("A user with this email already exists: " + email);
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(email);
        String password = generateRandomPassword();
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setProvider(AuthProvider.LOCAL);

        String[] nameParts = name.split(" ", 2);
        newUser.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
        newUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        newUser.setEnabled(true);

        Role developerRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER)
                .orElseThrow(() -> new RuntimeException("Error: ROLE_DEVELOPER not found. Please ensure roles are initialized in the database."));

        Set<Role> roles = new HashSet<>();
        roles.add(developerRole);
        newUser.setRoles(roles);
        User savedUser = userRepository.save(newUser);

        Developer developer = new Developer(name, email, skills);
        developer.setUser(savedUser);
        return developerRepository.save(developer);

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'all'")
    public List<Developer> getAllDevelopers() {
        return developerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "#id")
    public Developer getDeveloperById(Long id) {
        return developerRepository.findById(id).orElseThrow(() -> new DeveloperNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'email_' + #email")
    public Optional<Developer> getDeveloperByEmail(String email) {
        return developerRepository.findByEmail(email);
    }

    @Override
    @Auditable(action = ActionType.UPDATE, entityType = EntityType.DEVELOPER)
    @Caching(evict = {
            @CacheEvict(value = "developers", key = "#id"),
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "developers", key = "'email_' + #email"),
            @CacheEvict(value = "developerStats", allEntries = true)
    })
    public Developer updateDeveloper(Long id, String name, String email, String skills) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException(id));

        if (!developer.getEmail().equals(email)) {
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyExistsException("A user with this email already exists: " + email);
            }
            User associatedUser = developer.getUser();
            if (associatedUser != null) {
                associatedUser.setEmail(email);
                associatedUser.setUsername(email);
                userRepository.save(associatedUser);
            }
        }

        developer.setName(name);
        developer.setEmail(email);
        developer.setSkills(skills);

        return developerRepository.save(developer);
    }

    @Override
    @Auditable(action = ActionType.DELETE, entityType = EntityType.DEVELOPER)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "developerStats", allEntries = true),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public void deleteDeveloper(Long id) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException(id));

        User associatedUser = developer.getUser();
        developerRepository.delete(developer);
         if (associatedUser != null) {
             userRepository.delete(associatedUser);
         }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'search_name_' + #name")
    public List<Developer> searchDevelopersByName(String name) {
        return developerRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'skill_' + #skill")
    public List<Developer> findDevelopersBySkill(String skill) {
        return developerRepository.findBySkillsContainingIgnoreCase(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email) || developerRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developerStats", key = "'total_count'")
    public long getTotalDeveloperCount() {
        return developerRepository.count();
    }
}
