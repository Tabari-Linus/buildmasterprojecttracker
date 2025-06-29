package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.annotation.Auditable;
import lii.buildmaster.projecttracker.exception.DeveloperNotFoundException;
import lii.buildmaster.projecttracker.exception.EmailAlreadyExistsException;
import lii.buildmaster.projecttracker.mapper.DeveloperMapper;
import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.DeveloperSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.entity.Role;
import lii.buildmaster.projecttracker.model.entity.User;
import lii.buildmaster.projecttracker.model.enums.*;
import lii.buildmaster.projecttracker.repository.jpa.*;
import lii.buildmaster.projecttracker.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeveloperMapper developerMapper;
    private final TaskRepository taskRepository;


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

        Developer dev = developerMapper.toEntity(dto);
        dev.setUser(user);
        developerRepository.save(dev);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'all'", unless = "#result.isEmpty()")
    public Page<DeveloperSummaryDto> getAllDevelopers(Pageable pageable) {
        Page<Developer> developerPage = developerRepository.findAll(pageable);
        List<DeveloperSummaryDto> dtoList = developerPage.getContent().stream()
                .map(this::mapDeveloperToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, developerPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "#id")
    public DeveloperResponseDto getDeveloperById(Long id) {
        Developer dev = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found with id: " + id));
        return mapDeveloperToResponseDtoWithCalculatedFields(dev);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'email_' + #email")
    public DeveloperResponseDto getDeveloperByEmail(String email) {
        Developer developer = developerRepository.findByEmail(email)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found with email: " + email));
        return mapDeveloperToResponseDtoWithCalculatedFields(developer);
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
        return mapDeveloperToResponseDtoWithCalculatedFields(savedDev);
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





        taskRepository.findByDeveloperId(id).forEach(task -> task.setDeveloper(null));


        if (developer.getUser() != null) {
            userRepository.delete(developer.getUser());
        }
        developerRepository.delete(developer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'search_name_' + #name", unless = "#result.isEmpty()")
    public List<DeveloperSummaryDto> searchDevelopersByName(String name) {
        List<Developer> developers = developerRepository.findByNameContainingIgnoreCase(name);
        return developers.stream()
                .map(this::mapDeveloperToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'skill_' + #skill", unless = "#result.isEmpty()")
    public List<DeveloperSummaryDto> findDevelopersBySkill(String skill) {
        List<Developer> developers = developerRepository.findBySkillsContainingIgnoreCase(skill);
        return developers.stream()
                .map(this::mapDeveloperToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "'active_task_count'", unless = "#result.isEmpty()")
    public Page<DeveloperSummaryDto> getDevelopersWithActiveTaskCount(Pageable pageable) {
        Page<Developer> developerPage = developerRepository.findAll(pageable);
        List<DeveloperSummaryDto> dtoList = developerPage.getContent().stream()
                .map(this::mapDeveloperToSummaryDtoWithCalculatedFields)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, developerPage.getTotalElements());
    }


    private DeveloperResponseDto mapDeveloperToResponseDtoWithCalculatedFields(Developer developer) {
        DeveloperResponseDto dto = developerMapper.toResponseDto(developer);
        dto.setTotalTaskCount(taskRepository.countByDeveloperId(developer.getId()));
        dto.setActiveTaskCount(taskRepository.countByDeveloperIdAndStatusIn(developer.getId(), Set.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS)));
        dto.setCompletedTaskCount(taskRepository.countByDeveloperIdAndStatus(developer.getId(), TaskStatus.DONE));
        return dto;
    }


    private DeveloperSummaryDto mapDeveloperToSummaryDtoWithCalculatedFields(Developer developer) {
        DeveloperSummaryDto dto = developerMapper.toSummaryDto(developer);
        dto.setActiveTaskCount(taskRepository.countByDeveloperIdAndStatusIn(developer.getId(), Set.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS)));
        return dto;
    }
}
