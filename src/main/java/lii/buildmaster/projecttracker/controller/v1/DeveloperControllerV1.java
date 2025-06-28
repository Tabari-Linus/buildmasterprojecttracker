package lii.buildmaster.projecttracker.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lii.buildmaster.projecttracker.mapper.DeveloperMapper;
import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.DeveloperSummaryDto;
import lii.buildmaster.projecttracker.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/developers")
@Tag(name = "Developers", description = "Developer management operations")
@RequiredArgsConstructor
public class DeveloperControllerV1 {

    private final DeveloperService developerService;
    private final DeveloperMapper developerMapper;

    @GetMapping
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<Page<DeveloperSummaryDto>> getAllDevelopers(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        return ResponseEntity.ok(developerService.getAllDevelopers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@security.canViewAllDevelopers() or @security.canModifyDeveloper(#id)")
    public ResponseEntity<DeveloperResponseDto> getDeveloperById(@PathVariable Long id) {
        return ResponseEntity.ok(developerService.getDeveloperById(id));
    }

    @GetMapping("/email")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<DeveloperResponseDto> getDeveloperByEmail(@RequestParam String email) {

        DeveloperResponseDto developerDto = developerService.getDeveloperByEmail(email);
        return ResponseEntity.ok(developerDto);
    }


    @PutMapping("/{id}")
    @PreAuthorize("@security.canModifyDeveloper(#id)")
    public ResponseEntity<DeveloperResponseDto> updateDeveloper(@PathVariable Long id,
                                                                @Valid @RequestBody DeveloperRequestDto dto) {
        DeveloperResponseDto updated = developerService.updateDeveloper(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long id) {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<List<DeveloperSummaryDto>> searchByName(@RequestParam String name) {

        List<DeveloperSummaryDto> developerDtos = developerService.searchDevelopersByName(name);
        return ResponseEntity.ok(developerDtos);
    }

    @GetMapping("/skill")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<List<DeveloperSummaryDto>> searchBySkill(@RequestParam String skill) {

        List<DeveloperSummaryDto> developerDtos = developerService.findDevelopersBySkill(skill);
        return ResponseEntity.ok(developerDtos);
    }

    @GetMapping("/email-check")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        boolean isTaken = developerService.isEmailTaken(email);
        return ResponseEntity.ok(
                java.util.Map.of(
                        "email", email,
                        "available", !isTaken,
                        "message", isTaken ? "Email is already taken" : "Email is available"
                )
        );
    }

    @GetMapping("/stats/total-count")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<?> getTotalDeveloperCount() {
        long count = developerService.getTotalDeveloperCount();
        return ResponseEntity.ok(java.util.Map.of("totalCount", count));
    }

    @GetMapping("/stats/active-task-count")
    @PreAuthorize("@security.canViewAllDevelopers()")
    public ResponseEntity<List<DeveloperSummaryDto>> getDevelopersWithActiveTaskCount(
            @PageableDefault(size = 10, sort = "activeTaskCount,desc") Pageable pageable) {
        Page<DeveloperSummaryDto> developersPage = developerService.getDevelopersWithActiveTaskCount(pageable);
        return ResponseEntity.ok(developersPage.getContent());
    }
}