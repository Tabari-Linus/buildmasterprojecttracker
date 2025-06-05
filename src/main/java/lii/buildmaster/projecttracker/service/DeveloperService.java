package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.entity.Developer;

import java.util.List;
import java.util.Optional;

public interface DeveloperService {

    Developer createDeveloper(String name, String email, String skills);

    List<Developer> getAllDevelopers();

    Optional<Developer> getDeveloperById(Long id);

    Optional<Developer> getDeveloperByEmail(String email);

    Developer updateDeveloper(Long id, String name, String email, String skills);

    void deleteDeveloper(Long id);

    List<Developer> searchDevelopersByName(String name);

    List<Developer> findDevelopersBySkill(String skill);

    boolean isEmailTaken(String email);

    long getTotalDeveloperCount();
}
