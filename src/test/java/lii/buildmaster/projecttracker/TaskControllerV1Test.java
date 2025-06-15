package lii.buildmaster.projecttracker;

import lii.buildmaster.projecttracker.controller.v1.TaskControllerV1;
import lii.buildmaster.projecttracker.model.dto.request.TaskRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.TaskResponseDto;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lii.buildmaster.projecttracker.service.TaskService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerV1Test {

    @Mock private TaskService taskService;

    @InjectMocks
    private TaskControllerV1 taskController;

    private TaskRequestDto testRequestDto;
    private TaskResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testRequestDto = new TaskRequestDto();
        testRequestDto.setTitle("Test Task");
        testRequestDto.setDescription("Test Description");
        testRequestDto.setStatus(TaskStatus.TODO);
        testRequestDto.setDueDate(LocalDateTime.now().plusDays(7));

        testResponseDto = new TaskResponseDto();
        testResponseDto.setId(1L);
        testResponseDto.setTitle("Test Task");
        testResponseDto.setDescription("Test Description");
        testResponseDto.setStatus(TaskStatus.TODO);
        testResponseDto.setDueDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void getAllTasks_Success() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<TaskResponseDto> taskPage = new PageImpl<>(List.of(testResponseDto));
        when(taskService.getAllTasks(pageable)).thenReturn(taskPage);


        var response = taskController.getAllTasks(pageable);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<TaskResponseDto> page = response.getBody();
        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("Test Task", page.getContent().get(0).getTitle());
    }

    @Test
    void getTaskById_Success() {

        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(testResponseDto);


        var response = taskController.getTaskById(taskId);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        TaskResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("Test Task", responseDto.getTitle());
    }

    @Test
    void createTask_Success() {

        when(taskService.createTask(testRequestDto)).thenReturn(testResponseDto);


        var response = taskController.createTask(testRequestDto);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        TaskResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("Test Task", responseDto.getTitle());
        verify(taskService).createTask(testRequestDto);
    }

    @Test
    void updateTask_Success() {

        Long taskId = 1L;
        when(taskService.updateTask(
                eq(taskId),
                eq(testRequestDto.getTitle()),
                eq(testRequestDto.getDescription()),
                eq(testRequestDto.getStatus()),
                eq(testRequestDto.getDueDate())
        )).thenReturn(testResponseDto);


        var response = taskController.updateTask(taskId, testRequestDto);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        TaskResponseDto responseDto = response.getBody();
        assertNotNull(responseDto);
        assertEquals("Test Task", responseDto.getTitle());
        verify(taskService).updateTask(
                taskId,
                testRequestDto.getTitle(),
                testRequestDto.getDescription(),
                testRequestDto.getStatus(),
                testRequestDto.getDueDate()
        );
    }

    @Test
    void deleteTask_Success() {

        Long taskId = 1L;
        doNothing().when(taskService).deleteTask(taskId);


        var response = taskController.deleteTask(taskId);


        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taskService).deleteTask(taskId);
    }

    @Test
    void assignTaskToDeveloper_Success() {

        Long taskId = 1L;
        Long developerId = 2L;
        doNothing().when(taskService).assignTaskToDeveloper(eq(taskId), eq(developerId));

        var response = taskController.assignTaskToDeveloper(taskId, developerId);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskService).assignTaskToDeveloper(taskId, developerId);
    }
}