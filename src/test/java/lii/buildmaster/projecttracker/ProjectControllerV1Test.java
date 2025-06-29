package lii.buildmaster.projecttracker;

import lii.buildmaster.projecttracker.controller.v1.ProjectControllerV1;
import lii.buildmaster.projecttracker.mapper.ProjectMapper;
import lii.buildmaster.projecttracker.model.dto.request.ProjectRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.dto.response.ApiResponse;
import lii.buildmaster.projecttracker.model.dto.summary.ProjectSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lii.buildmaster.projecttracker.service.ProjectService;
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
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerV1Test {

    @Mock private ProjectService projectService;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks private ProjectControllerV1 controller;

    private Project project;
    private ProjectRequestDto requestDto;
    private ProjectResponseDto responseDto;
    private ProjectSummaryDto summaryDto;

    @BeforeEach
    void setup() {
        project = new Project("Test Project", "Desc", LocalDateTime.now().plusDays(10), ProjectStatus.PLANNING);
        project.setId(1L);

        requestDto = new ProjectRequestDto();
        requestDto.setName("Test Project");
        requestDto.setDescription("Desc");
        requestDto.setDeadline(LocalDateTime.now().plusDays(10));
        requestDto.setStatus(ProjectStatus.PLANNING);

        responseDto = new ProjectResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Test Project");
        responseDto.setDescription("Desc");
        responseDto.setStatus(ProjectStatus.PLANNING);

        summaryDto = new ProjectSummaryDto();
        summaryDto.setId(1L);
        summaryDto.setName("Test Project");
        summaryDto.setStatus(ProjectStatus.PLANNING);
    }

    @Test
    void getAllProjects_returnsPagedSummaryDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectSummaryDto> summaryPage = new PageImpl<>(List.of(summaryDto));

        when(projectService.getAllProjects(pageable)).thenReturn(summaryPage);

        ResponseEntity<ApiResponse<Page<ProjectSummaryDto>>> response = controller.getAllProjects(pageable);

        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(projectService).getAllProjects(pageable);
    }

    @Test
    void getProjectById_returnsDto() {
        when(projectService.getProjectById(1L)).thenReturn(responseDto);

        ResponseEntity<ApiResponse<ProjectResponseDto>> response = controller.getProjectById(1L);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Test Project", response.getBody().getData().getName());
        verify(projectService).getProjectById(1L);
    }

    @Test
    void createProject_returnsCreatedDto() {
        when(projectService.createProject(any(), any(), any(), any())).thenReturn(responseDto);

        ResponseEntity<ApiResponse<ProjectResponseDto>> response = controller.createProject(requestDto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Test Project", response.getBody().getData().getName());
    }

    @Test
    void updateProject_returnsUpdatedDto() {
        when(projectService.updateProject(eq(1L), any(), any(), any(), any())).thenReturn(responseDto);

        ResponseEntity<ApiResponse<ProjectResponseDto>> response = controller.updateProject(1L, requestDto);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Test Project", response.getBody().getData().getName());
    }

    @Test
    void deleteProject_returnsOkMessage() {
        doNothing().when(projectService).deleteProject(1L);

        ResponseEntity<ApiResponse<String>> response = controller.deleteProject(1L);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Project deleted successfully", response.getBody().message());
        verify(projectService).deleteProject(1L);
    }

    @Test
    void getProjectsByStatus_returnsFilteredProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        when(projectService.getProjectsByStatus(ProjectStatus.PLANNING, pageable))
                .thenReturn(new PageImpl<>(List.of(summaryDto)));

        ResponseEntity<ApiResponse<Page<ProjectSummaryDto>>> response = controller.getProjectsByStatus(pageable, ProjectStatus.PLANNING);

        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(projectService).getProjectsByStatus(ProjectStatus.PLANNING, pageable);
    }

    @Test
    void searchProjects_returnsMatchingProjects() {
        when(projectService.searchProjectsByName("Test")).thenReturn(List.of(summaryDto));

        ResponseEntity<ApiResponse<List<ProjectSummaryDto>>> response = controller.searchProjects("Test");

        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getOverdueProjects_returnsOverdueList() {
        when(projectService.getOverdueProjects()).thenReturn(List.of(summaryDto));

        ResponseEntity<ApiResponse<List<ProjectSummaryDto>>> response = controller.getOverdueProjects();

        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void markAsCompleted_returnsCompletedDto() {
        responseDto.setStatus(ProjectStatus.COMPLETED);
        when(projectService.markAsCompleted(1L)).thenReturn(responseDto);

        ResponseEntity<ApiResponse<ProjectResponseDto>> response = controller.markAsCompleted(1L);

        assertNotNull(response.getBody());
        assertEquals(ProjectStatus.COMPLETED, response.getBody().getData().getStatus());
    }

    @Test
    void getProjectCountsByStatus_returnsAllCounts() {
        Map<ProjectStatus, Long> counts = new HashMap<>();
        counts.put(ProjectStatus.PLANNING, 2L);
        counts.put(ProjectStatus.IN_PROGRESS, 4L);
        counts.put(ProjectStatus.COMPLETED, 6L);
        counts.put(ProjectStatus.ON_HOLD, 0L);

        when(projectService.getProjectCountsByStatus()).thenReturn(counts);

        ResponseEntity<ApiResponse<Map<ProjectStatus, Long>>> response = controller.getProjectCountsByStatus();

        assertNotNull(response.getBody());
        Map<ProjectStatus, Long> data = response.getBody().getData();
        assertEquals(2L, data.get(ProjectStatus.PLANNING));
        assertEquals(4L, data.get(ProjectStatus.IN_PROGRESS));
        assertEquals(6L, data.get(ProjectStatus.COMPLETED));
        assertEquals(0L, data.get(ProjectStatus.ON_HOLD));
    }
}