package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.service.AuditLogService;
import lii.buildmaster.projecttracker.service.DeveloperService;
import lii.buildmaster.projecttracker.util.AuditUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;


    public DeveloperServiceImpl(DeveloperRepository developerRepository, AuditLogService auditLogService,
                                AuditUtil auditUtil) {
        this.developerRepository = developerRepository;
    }

    @Override
    @Auditable(action = ActionType.CREATE, entityType = EntityType.DEVELOPER)
    @Caching(evict = {
            @CacheEvict(value = "developers", allEntries = true),
            @CacheEvict(value = "developerStats", allEntries = true)
    })
    public Developer createDeveloper(String name, String email, String skills) {

        if (developerRepository.existsByEmail(email)) {
            throw new RuntimeException("Developer with email " + email + " already exists");
        }

        Developer developer = new Developer(name, email, skills);
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
    public Optional<Developer> getDeveloperById(Long id) {
        return developerRepository.findById(id);
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
            @CacheEvict(value = "developers", key = "'all'"),
            @CacheEvict(value = "developers", key = "'email_' + #email"),
            @CacheEvict(value = "developerStats", allEntries = true)
    })
    public Developer updateDeveloper(Long id, String name, String email, String skills) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Developer not found with id: " + id));


        Optional<Developer> existingDeveloper = developerRepository.findByEmail(email);
        if (existingDeveloper.isPresent() && !existingDeveloper.get().getId().equals(id)) {
            throw new RuntimeException("Email " + email + " is already taken by another developer");
        }

        developer.setName(name);
        developer.setEmail(email);
        developer.setSkills(skills);

        return developerRepository.save(developer);

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
                .orElseThrow(() -> new RuntimeException("Developer not found with id: " + id));

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
        return developerRepository.findDevelopersBySkill(skill);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developerStats", key = "'email_exists_' + #email")
    public boolean isEmailTaken(String email) {
        return developerRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developerStats", key = "'total_count'")
    public long getTotalDeveloperCount() {
        return developerRepository.count();
    }
}
