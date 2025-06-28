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

    Page<DeveloperSummaryDto> getAllDevelopers(Pageable pageable);

    DeveloperResponseDto getDeveloperById(Long id);

    DeveloperResponseDto getDeveloperByEmail(String email);

    DeveloperResponseDto updateDeveloper(Long id, DeveloperRequestDto dto);

    void deleteDeveloper(Long id);

    List<DeveloperSummaryDto> searchDevelopersByName(String name);

    List<DeveloperSummaryDto> findDevelopersBySkill(String skill);

    boolean isEmailTaken(String email);

    long getTotalDeveloperCount();

    Page<DeveloperSummaryDto> getDevelopersWithActiveTaskCount(Pageable pageable);
}