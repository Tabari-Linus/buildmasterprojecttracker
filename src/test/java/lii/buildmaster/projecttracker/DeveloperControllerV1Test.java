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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeveloperControllerV1Test {

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
        testDeveloper.setSkills("Java, Spring Boot, Angular");

        testRequestDto = new DeveloperRequestDto();
        testRequestDto.setName("John Doe");
        testRequestDto.setEmail("john.doe@example.com");
        testRequestDto.setSkills("Java, Spring Boot, Angular");

        testResponseDto = new DeveloperResponseDto();
        testResponseDto.setId(1L);
        testResponseDto.setName("John Doe");
        testResponseDto.setEmail("john.doe@example.com");
        testResponseDto.setSkills("Java, Spring Boot, Angular");

        testSummaryDto = new DeveloperSummaryDto();
        testSummaryDto.setId(1L);
        testSummaryDto.setName("John Doe");
        testSummaryDto.setEmail("john.doe@example.com");
    }

    @Test
    void getAllDevelopers_Success() {

        Pageable pageable = PageRequest.of(0, 10);
        List<Developer> developers = List.of(testDeveloper);
        when(developerService.getAllDevelopers()).thenReturn(developers);
        when(developerMapper.toSummaryDto(testDeveloper)).thenReturn(testSummaryDto);


        var response = developerController.getAllDevelopers(pageable);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<DeveloperSummaryDto> page = response.getBody();
        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("John Doe", page.getContent().get(0).getName());
    }

    @Test
    void getDeveloperById_Success() {

        Long developerId = 1L;
        when(developerService.getDeveloperById(developerId)).thenReturn(testDeveloper);
        when(developerMapper.toResponseDto(testDeveloper)).thenReturn(testResponseDto);


        var response = developerController.getDeveloperById(developerId);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        DeveloperResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("John Doe", responseDto.getName());
    }

    @Test
    void getDeveloperByEmail_Success() {

        String email = "john.doe@example.com";
        when(developerService.getDeveloperByEmail(email)).thenReturn(Optional.of(testDeveloper));
        when(developerMapper.toResponseDto(testDeveloper)).thenReturn(testResponseDto);


        var response = developerController.getDeveloperByEmail(email);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        DeveloperResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("john.doe@example.com", responseDto.getEmail());
    }

    @Test
    void getDeveloperByEmail_NotFound() {

        String email = "notfound@example.com";
        when(developerService.getDeveloperByEmail(email)).thenReturn(Optional.empty());


        var response = developerController.getDeveloperByEmail(email);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createDeveloper_Success() {

        when(developerService.createDeveloper(
                testRequestDto.getName(),
                testRequestDto.getEmail(),
                testRequestDto.getSkills()
        )).thenReturn(testDeveloper);
        when(developerMapper.toResponseDto(testDeveloper)).thenReturn(testResponseDto);


        var response = developerController.createDeveloper(testRequestDto);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        DeveloperResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("John Doe", responseDto.getName());
        verify(developerService).createDeveloper(
                testRequestDto.getName(),
                testRequestDto.getEmail(),
                testRequestDto.getSkills()
        );
    }

    @Test
    void createDeveloper_Conflict() {

        when(developerService.createDeveloper(
                anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("Email already exists"));


        var response = developerController.createDeveloper(testRequestDto);


        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateDeveloper_Success() {

        Long developerId = 1L;
        when(developerService.updateDeveloper(
                eq(developerId),
                eq(testRequestDto.getName()),
                eq(testRequestDto.getEmail()),
                eq(testRequestDto.getSkills())
        )).thenReturn(testDeveloper);
        when(developerMapper.toResponseDto(testDeveloper)).thenReturn(testResponseDto);


        var response = developerController.updateDeveloper(developerId, testRequestDto);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        DeveloperResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("John Doe", responseDto.getName());
    }

    @Test
    void updateDeveloper_NotFound() {

        Long developerId = 999L;
        when(developerService.updateDeveloper(
                eq(developerId), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("Developer not found"));


        var response = developerController.updateDeveloper(developerId, testRequestDto);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateDeveloper_Conflict() {

        Long developerId = 1L;
        when(developerService.updateDeveloper(
                eq(developerId), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("Email already exists"));


        var response = developerController.updateDeveloper(developerId, testRequestDto);


        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteDeveloper_Success() {

        Long developerId = 1L;
        doNothing().when(developerService).deleteDeveloper(developerId);


        var response = developerController.deleteDeveloper(developerId);


        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(developerService).deleteDeveloper(developerId);
    }

    @Test
    void deleteDeveloper_NotFound() {

        Long developerId = 999L;
        doThrow(new RuntimeException("Developer not found"))
                .when(developerService).deleteDeveloper(developerId);


        var response = developerController.deleteDeveloper(developerId);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void searchDevelopers_Success() {

        String searchName = "John";
        List<Developer> developers = List.of(testDeveloper);
        when(developerService.searchDevelopersByName(searchName)).thenReturn(developers);
        when(developerMapper.toSummaryDto(testDeveloper)).thenReturn(testSummaryDto);


        var response = developerController.searchDevelopers(searchName);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<DeveloperSummaryDto> searchResults = response.getBody();
        assertNotNull(searchResults);
        assertEquals(1, searchResults.size());
        assertEquals("John Doe", searchResults.get(0).getName());
    }

    @Test
    void findDevelopersBySkill_Success() {

        String skill = "Java";
        List<Developer> developers = List.of(testDeveloper);
        when(developerService.findDevelopersBySkill(skill)).thenReturn(developers);
        when(developerMapper.toSummaryDto(testDeveloper)).thenReturn(testSummaryDto);


        var response = developerController.findDevelopersBySkill(skill);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<DeveloperSummaryDto> skillResults = response.getBody();
        assertNotNull(skillResults);
        assertEquals(1, skillResults.size());
        assertEquals("John Doe", skillResults.get(0).getName());
    }

    @Test
    void checkEmailAvailability_Available() {

        String email = "available@example.com";
        when(developerService.isEmailTaken(email)).thenReturn(false);


        var response = developerController.checkEmailAvailability(email);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> result = response.getBody();
        assertNotNull(result);
        assertEquals(email, result.get("email"));
        assertEquals(true, result.get("available"));
        assertEquals("Email is available", result.get("message"));
    }

    @Test
    void checkEmailAvailability_Taken() {

        String email = "taken@example.com";
        when(developerService.isEmailTaken(email)).thenReturn(true);


        var response = developerController.checkEmailAvailability(email);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> result = response.getBody();
        assertNotNull(result);
        assertEquals(email, result.get("email"));
        assertEquals(false, result.get("available"));
        assertEquals("Email is already taken", result.get("message"));
    }

    @Test
    void getTotalDeveloperCount_Success() {

        long expectedCount = 25L;
        when(developerService.getTotalDeveloperCount()).thenReturn(expectedCount);


        var response = developerController.getTotalDeveloperCount();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Long> result = response.getBody();
        assertNotNull(result);
        assertEquals(expectedCount, result.get("totalCount"));
    }
}