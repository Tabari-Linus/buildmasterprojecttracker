package lii.buildmaster.projecttracker.service;

import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.DeveloperSummaryDto;
import lii.buildmaster.projecttracker.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeveloperService {

    void createDeveloper(DeveloperRequestDto dto, User user);


    Page<Developer> getAllDevelopers(Pageable pageable);

    DeveloperResponseDto getDeveloperById(Long id);

    Developer getDeveloperByEmail(String email);

    DeveloperResponseDto updateDeveloper(Long id, DeveloperRequestDto dto);

    void deleteDeveloper(Long id);

    List<Developer> searchDevelopersByName(String name);

    List<Developer> findDevelopersBySkill(String skill);

    boolean isEmailTaken(String email);

    long getTotalDeveloperCount();
}
