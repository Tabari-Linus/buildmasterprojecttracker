package lii.buildmaster.projecttracker.service.impl;

import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.repository.jpa.DeveloperRepository;
import lii.buildmaster.projecttracker.service.DeveloperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;

    public DeveloperServiceImpl(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @Override
    public Developer createDeveloper(String name, String email, String skills) {

        if (developerRepository.existsByEmail(email)) {
            throw new RuntimeException("Developer with email " + email + " already exists");
        }

        Developer developer = new Developer(name, email, skills);
        return developerRepository.save(developer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Developer> getAllDevelopers() {
        return developerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Developer> getDeveloperById(Long id) {
        return developerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Developer> getDeveloperByEmail(String email) {
        return developerRepository.findByEmail(email);
    }

    @Override
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
    public void deleteDeveloper(Long id) {
        if (!developerRepository.existsById(id)) {
            throw new RuntimeException("Developer not found with id: " + id);
        }
        developerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Developer> searchDevelopersByName(String name) {
        return developerRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Developer> findDevelopersBySkill(String skill) {
        return developerRepository.findDevelopersBySkill(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return developerRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDeveloperCount() {
        return developerRepository.count();
    }
}
