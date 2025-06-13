package lii.buildmaster.projecttracker.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lii.buildmaster.projecttracker.mapper.DeveloperMapper;
import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.DeveloperSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.service.DeveloperService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/developers")
@Tag(name = "Developers", description = "Developer management operations including CRUD, search by skills, and task statistics")
public class DeveloperControllerV1 {

    private final DeveloperService developerService;
    private final DeveloperMapper developerMapper;

    public DeveloperControllerV1(DeveloperService developerService, DeveloperMapper developerMapper) {
        this.developerService = developerService;
        this.developerMapper = developerMapper;
    }

    @GetMapping
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<Page<DeveloperSummaryDto>> getAllDevelopers(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        List<Developer> developers = developerService.getAllDevelopers();
        List<DeveloperSummaryDto> developerDtos = developers.stream()
                .map(developerMapper::toSummaryDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), developerDtos.size());
        List<DeveloperSummaryDto> pageContent = developerDtos.subList(start, end);

        Page<DeveloperSummaryDto> page = new PageImpl<>(pageContent, pageable, developerDtos.size());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@security.canViewAllDevelopers() or @security.canModifyDeveloper(#id)")
    public ResponseEntity<DeveloperResponseDto> getDeveloperById(@PathVariable Long id) {
        Developer developer = developerService.getDeveloperById(id);
        return ResponseEntity.ok(developerMapper.toResponseDto(developer));
    }


    @GetMapping("/email")
    @PreAuthorize("@security.canViewAllDevelopers() or @security.canModifyDeveloper(#id)")
    public ResponseEntity<DeveloperResponseDto> getDeveloperByEmail(@RequestParam String email) {
        Optional<Developer> developer = developerService.getDeveloperByEmail(email);

        if (developer.isPresent()) {
            DeveloperResponseDto responseDto = developerMapper.toResponseDto(developer.get());
            return ResponseEntity.ok(responseDto);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DeveloperResponseDto> createDeveloper(@Valid @RequestBody DeveloperRequestDto requestDto) {
        try {
            Developer developer = developerService.createDeveloper(
                    requestDto.getName(),
                    requestDto.getEmail(),
                    requestDto.getSkills()
            );

            DeveloperResponseDto responseDto = developerMapper.toResponseDto(developer);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@security.canModifyDeveloper(#id)")
    public ResponseEntity<DeveloperResponseDto> updateDeveloper(
            @PathVariable Long id,
            @Valid @RequestBody DeveloperRequestDto requestDto) {

        try {
            Developer updatedDeveloper = developerService.updateDeveloper(
                    id,
                    requestDto.getName(),
                    requestDto.getEmail(),
                    requestDto.getSkills()
            );

            DeveloperResponseDto responseDto = developerMapper.toResponseDto(updatedDeveloper);
            return ResponseEntity.ok(responseDto);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long id) {
        try {
            developerService.deleteDeveloper(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<List<DeveloperSummaryDto>> searchDevelopers(@RequestParam String name) {
        List<Developer> developers = developerService.searchDevelopersByName(name);
        List<DeveloperSummaryDto> developerDtos = developers.stream()
                .map(developerMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(developerDtos);
    }

    @GetMapping("/skill")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<List<DeveloperSummaryDto>> findDevelopersBySkill(@RequestParam String skill) {
        List<Developer> developers = developerService.findDevelopersBySkill(skill);
        List<DeveloperSummaryDto> developerDtos = developers.stream()
                .map(developerMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(developerDtos);
    }

    @GetMapping("/email-check")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<java.util.Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        boolean isTaken = developerService.isEmailTaken(email);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("email", email);
        response.put("available", !isTaken);
        response.put("message", isTaken ? "Email is already taken" : "Email is available");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total-count")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<java.util.Map<String, Long>> getTotalDeveloperCount() {
        long count = developerService.getTotalDeveloperCount();
        java.util.Map<String, Long> response = new java.util.HashMap<>();
        response.put("totalCount", count);

        return ResponseEntity.ok(response);
    }


}