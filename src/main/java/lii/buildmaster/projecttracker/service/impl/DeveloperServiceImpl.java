package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.exception.DeveloperNotFoundException;
import lii.buildmaster.projecttracker.exception.EmailAlreadyExistsException;
import lii.buildmaster.projecttracker.mapper.DeveloperMapper;
import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.*;
import lii.buildmaster.projecttracker.repository.jpa.*;
import lii.buildmaster.projecttracker.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final DeveloperMapper developerMapper;


    @Override
    @Auditable(action = ActionType.CREATE, entityType = EntityType.DEVELOPER)
    @Caching(evict = {
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "developerStats", allEntries = true)
    })
    public void createDeveloper(DeveloperRequestDto dto, User user) {
        if (developerRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Developer with email already exists: " + dto.getEmail());
        }

        Developer dev = new Developer(dto.getName(), dto.getEmail(), dto.getSkills());
        dev.setUser(user);
        developerRepository.save(dev);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'all'", unless = "#result.isEmpty()")
    public Page<Developer> getAllDevelopers(Pageable pageable) {
        return developerRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "#id")
    public DeveloperResponseDto getDeveloperById(Long id) {
        Developer dev = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found with id: " + id));
        return new DeveloperResponseDto(dev.getId(), dev.getName(), dev.getEmail(), dev.getSkills());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'email_' + #email")
    public Developer getDeveloperByEmail(String email) {
        return developerRepository.findByEmail(email)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found with email: " + email));
    }

    @Override
    @Auditable(action = ActionType.UPDATE, entityType = EntityType.DEVELOPER)
    @Caching(evict = {
            @CacheEvict(value = "developers", key = "#id"),
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "developerStats", allEntries = true)
    })
    public DeveloperResponseDto updateDeveloper(Long id, DeveloperRequestDto dto) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found with id: " + id));

        if (!developer.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("User with email already exists: " + dto.getEmail());
        }

        Optional<User> user = userRepository.findById(developer.getUserId());
        User existingUser = user.orElse(null);
        assert existingUser != null;
        if (!existingUser.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("User with email already exists: " + dto.getEmail());
        }
        if (!existingUser.getEmail().equals(dto.getEmail())) {

            existingUser.setEmail(dto.getEmail());
            userRepository.save(existingUser);
        }

        developer.setName(dto.getName());
        developer.setEmail(dto.getEmail());
        developer.setSkills(dto.getSkills());

        Developer savedDev = developerRepository.save(developer);
        return developerMapper.toResponseDto(savedDev);
    }


    @Override
    @Auditable(action = ActionType.DELETE, entityType = EntityType.DEVELOPER)
    @Caching(evict = {
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "developerStats", allEntries = true),
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskStats", allEntries = true)
    })
    public void deleteDeveloper(Long id) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found with id: " + id));
        if (developer == null) {
            throw new DeveloperNotFoundException("Developer not found with id: " + id);
        }

        if (developer.getAssignedTask() != null) {
            throw new IllegalStateException("Cannot delete developer with assigned tasks.");
        }

        if (developer.getUser() != null) {
            userRepository.delete(developer.getUser());
        }
        developerRepository.delete(developer);
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
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email) || developerRepository.existsByEmail(email);
    }

    @Override
    @Cacheable(value = "developerStats", key = "'total_count'")
    public long getTotalDeveloperCount() {
        return developerRepository.count();
    }
}
