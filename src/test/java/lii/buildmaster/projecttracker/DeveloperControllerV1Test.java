package lii.buildmaster.projecttracker;

import lii.buildmaster.projecttracker.controller.v1.DeveloperControllerV1;
import lii.buildmaster.projecttracker.mapper.DeveloperMapper;
import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.DeveloperSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.service.DeveloperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeveloperControllerV1TestRefactored {

    @Mock private DeveloperService developerService;
    @Mock private DeveloperMapper developerMapper;

    @InjectMocks
    private DeveloperControllerV1 developerController;

    private Developer testDeveloper;
    private DeveloperRequestDto testRequestDto;
    private DeveloperResponseDto testResponseDto;
    private DeveloperSummaryDto testSummaryDto;

    @BeforeEach
    void setUp() {
        testDeveloper = new Developer();
        testDeveloper.setId(1L);
        testDeveloper.setName("John Doe");
        testDeveloper.setEmail("john.doe@example.com");
        testDeveloper.setSkills("Java, Spring Boot");

        testRequestDto = new DeveloperRequestDto();
        testRequestDto.setName("John Doe");
        testRequestDto.setEmail("john.doe@example.com");
        testRequestDto.setSkills("Java, Spring Boot");

        testResponseDto = new DeveloperResponseDto();
        testResponseDto.setId(1L);
        testResponseDto.setName("John Doe");
        testResponseDto.setEmail("john.doe@example.com");
        testResponseDto.setSkills("Java, Spring Boot");

        testSummaryDto = new DeveloperSummaryDto();
        testSummaryDto.setId(1L);
        testSummaryDto.setName("John Doe");
        testSummaryDto.setEmail("john.doe@example.com");
    }
    @Test
    void getAllDevelopers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Developer> developers = List.of(testDeveloper);
        when(developerService.getAllDevelopers(pageable)).thenReturn(new PageImpl<>(developers));
        when(developerMapper.toSummaryDto(testDeveloper)).thenReturn(testSummaryDto);

        var response = developerController.getAllDevelopers(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getDeveloperById_Success() {
        when(developerService.getDeveloperById(1L)).thenReturn(testResponseDto);
        when(developerMapper.toResponseDto(testDeveloper)).thenReturn(testResponseDto);

        var response = developerController.getDeveloperById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void getDeveloperByEmail_Found() {
        when(developerService.getDeveloperByEmail("john.doe@example.com"))
                .thenReturn(testDeveloper);
        when(developerMapper.toResponseDto(testDeveloper)).thenReturn(testResponseDto);

        var response = developerController.getDeveloperByEmail("john.doe@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
    }

    @Test
    void getDeveloperByEmail_NotFound() {
        when(developerService.getDeveloperByEmail("unknown@example.com"))
                .thenReturn(null);

        var response = developerController.getDeveloperByEmail("unknown@example.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    

    @Test
    void updateDeveloper_Success() {
        when(developerService.updateDeveloper(1L, testRequestDto)).thenReturn(testResponseDto);
        when(developerMapper.toResponseDto(testDeveloper)).thenReturn(testResponseDto);

        var response = developerController.updateDeveloper(1L, testRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void deleteDeveloper_Success() {
        doNothing().when(developerService).deleteDeveloper(1L);

        var response = developerController.deleteDeveloper(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void searchByName_Success() {
        when(developerService.searchDevelopersByName("John")).thenReturn(List.of(testDeveloper));
        when(developerMapper.toSummaryDto(testDeveloper)).thenReturn(testSummaryDto);

        var response = developerController.searchByName("John");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void searchBySkill_Success() {
        when(developerService.findDevelopersBySkill("Java")).thenReturn(List.of(testDeveloper));
        when(developerMapper.toSummaryDto(testDeveloper)).thenReturn(testSummaryDto);

        var response = developerController.searchBySkill("Java");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void checkEmailAvailability_Available() {
        when(developerService.isEmailTaken("new@example.com")).thenReturn(false);

        var response = developerController.checkEmailAvailability("new@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("available"));
    }

    @Test
    void checkEmailAvailability_Taken() {
        when(developerService.isEmailTaken("taken@example.com")).thenReturn(true);

        var response = developerController.checkEmailAvailability("taken@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(false, body.get("available"));
    }

    @Test
    void getTotalDeveloperCount_Success() {
        when(developerService.getTotalDeveloperCount()).thenReturn(10L);

        var response = developerController.getTotalDeveloperCount();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(10L, body.get("totalCount"));
    }
}
