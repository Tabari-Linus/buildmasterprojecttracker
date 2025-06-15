package lii.buildmaster.projecttracker;

import lii.buildmaster.projecttracker.controller.v1.ProjectControllerV1;
import lii.buildmaster.projecttracker.mapper.ProjectMapper;
import lii.buildmaster.projecttracker.model.dto.request.ProjectRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.ProjectSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerV1Test {

    @Mock private ProjectServiceImpl projectServiceImpl;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectControllerV1 projectController;

    private Project testProject;
    private ProjectRequestDto testRequestDto;
    private ProjectResponseDto testResponseDto;
    private ProjectSummaryDto testSummaryDto;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStatus(ProjectStatus.PLANNING);

        testRequestDto = new ProjectRequestDto();
        testRequestDto.setName("Test Project");
        testRequestDto.setDescription("Test Description");
        testRequestDto.setStatus(ProjectStatus.PLANNING);
        testRequestDto.setDeadline(LocalDateTime.now().plusDays(30));

        testResponseDto = new ProjectResponseDto();
        testResponseDto.setId(1L);
        testResponseDto.setName("Test Project");
        testResponseDto.setDescription("Test Description");
        testResponseDto.setStatus(ProjectStatus.PLANNING);

        testSummaryDto = new ProjectSummaryDto();
        testSummaryDto.setId(1L);
        testSummaryDto.setName("Test Project");
        testSummaryDto.setStatus(ProjectStatus.PLANNING);
    }

    @Test
    void getAllProjects_Success() {

        Pageable pageable = PageRequest.of(0, 10);
        List<Project> projects = List.of(testProject);
        when(projectServiceImpl.getAllProjects(pageable)).thenReturn(projects);
        when(projectMapper.toSummaryDto(testProject)).thenReturn(testSummaryDto);


        var response = projectController.getAllProjects(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<ProjectSummaryDto> page = response.getBody();
        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("Test Project", page.getContent().get(0).getName());
    }

    @Test
    void getProjectById_Success() {

        Long projectId = 1L;
        when(projectServiceImpl.getProjectById(projectId)).thenReturn(testResponseDto);


        var response = projectController.getProjectById(projectId);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProjectResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("Test Project", responseDto.getName());
    }

    @Test
    void createProject_Success() {

        when(projectServiceImpl.createProject(
                testRequestDto.getName(),
                testRequestDto.getDescription(),
                testRequestDto.getDeadline(),
                testRequestDto.getStatus()
        )).thenReturn(testProject);
        when(projectMapper.toResponseDto(testProject)).thenReturn(testResponseDto);


        var response = projectController.createProject(testRequestDto);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ProjectResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("Test Project", responseDto.getName());
        verify(projectServiceImpl).createProject(
                testRequestDto.getName(),
                testRequestDto.getDescription(),
                testRequestDto.getDeadline(),
                testRequestDto.getStatus()
        );
    }

    @Test
    void updateProject_Success() {

        Long projectId = 1L;
        when(projectServiceImpl.updateProject(
                eq(projectId),
                eq(testRequestDto.getName()),
                eq(testRequestDto.getDescription()),
                eq(testRequestDto.getDeadline()),
                eq(testRequestDto.getStatus())
        )).thenReturn(testProject);
        when(projectMapper.toResponseDto(testProject)).thenReturn(testResponseDto);


        var response = projectController.updateProject(projectId, testRequestDto);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProjectResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("Test Project", responseDto.getName());
    }

    @Test
    void updateProject_NotFound() {
        // Given
        Long projectId = 999L;
        when(projectServiceImpl.updateProject(
                eq(projectId),
                anyString(),
                anyString(),
                any(),
                any()
        )).thenThrow(new RuntimeException("Project not found"));


        var response = projectController.updateProject(projectId, testRequestDto);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteProject_Success() {

        Long projectId = 1L;
        doNothing().when(projectServiceImpl).deleteProject(projectId);


        var response = projectController.deleteProject(projectId);


        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(projectServiceImpl).deleteProject(projectId);
    }

    @Test
    void deleteProject_NotFound() {

        Long projectId = 999L;
        doThrow(new RuntimeException("Project not found"))
                .when(projectServiceImpl).deleteProject(projectId);


        var response = projectController.deleteProject(projectId);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProjectsByStatus_Success() {

        Pageable pageable = PageRequest.of(0, 10);
        ProjectStatus status = ProjectStatus.PLANNING;
        Page<ProjectResponseDto> projectPage = new PageImpl<>(List.of(testResponseDto));

        when(projectServiceImpl.getProjectsByStatus(status, pageable)).thenReturn(projectPage);
        when(projectMapper.toSummaryDto(any(ProjectResponseDto.class))).thenReturn(testSummaryDto);


        var response = projectController.getProjectsByStatus(pageable, status);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ProjectSummaryDto> projects = response.getBody();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertEquals(ProjectStatus.PLANNING, projects.get(0).getStatus());
    }

    @Test
    void searchProjects_Success() {

        String searchName = "Test";
        List<Project> projects = List.of(testProject);
        when(projectServiceImpl.searchProjectsByName(searchName)).thenReturn(projects);
        when(projectMapper.toSummaryDto(testProject)).thenReturn(testSummaryDto);


        var response = projectController.searchProjects(searchName);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ProjectSummaryDto> searchResults = response.getBody();
        assertNotNull(searchResults);
        assertEquals(1, searchResults.size());
        assertEquals("Test Project", searchResults.get(0).getName());
    }

    @Test
    void markAsCompleted_Success() {

        Long projectId = 1L;
        testProject.setStatus(ProjectStatus.COMPLETED);
        testResponseDto.setStatus(ProjectStatus.COMPLETED);

        when(projectServiceImpl.markAsCompleted(projectId)).thenReturn(testProject);
        when(projectMapper.toResponseDto(testProject)).thenReturn(testResponseDto);


        var response = projectController.markAsCompleted(projectId);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProjectResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals(ProjectStatus.COMPLETED, responseDto.getStatus());
    }

    @Test
    void markAsCompleted_NotFound() {

        Long projectId = 999L;
        when(projectServiceImpl.markAsCompleted(projectId))
                .thenThrow(new RuntimeException("Project not found"));


        var response = projectController.markAsCompleted(projectId);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProjectCountsByStatus_Success() {

        when(projectServiceImpl.getProjectCountByStatus(ProjectStatus.PLANNING)).thenReturn(5L);
        when(projectServiceImpl.getProjectCountByStatus(ProjectStatus.IN_PROGRESS)).thenReturn(3L);
        when(projectServiceImpl.getProjectCountByStatus(ProjectStatus.COMPLETED)).thenReturn(10L);
        when(projectServiceImpl.getProjectCountByStatus(ProjectStatus.ON_HOLD)).thenReturn(2L);


        var response = projectController.getProjectCountsByStatus();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<ProjectStatus, Long> counts = response.getBody();
        assertNotNull(counts);
        assertEquals(5L, counts.get(ProjectStatus.PLANNING));
        assertEquals(3L, counts.get(ProjectStatus.IN_PROGRESS));
        assertEquals(10L, counts.get(ProjectStatus.COMPLETED));
        assertEquals(2L, counts.get(ProjectStatus.ON_HOLD));
    }
}