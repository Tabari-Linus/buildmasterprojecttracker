package lii.buildmaster.projecttracker.mapper;

import lii.buildmaster.projecttracker.model.dto.request.TaskRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.TaskResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.TaskSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Task;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, DeveloperMapper.class})
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "developer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskRequestDto requestDto);

    @Mapping(target = "project", source = "project")
    @Mapping(target = "developer", source = "developer")
    @Mapping(target = "overdue", expression = "java(isOverdue(task))")
    @Mapping(target = "daysUntilDue", expression = "java(getDaysUntilDue(task))")
    TaskResponseDto toResponseDto(Task task);

    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "developerName", source = "developer.name")
    @Mapping(target = "overdue", expression = "java(isOverdue(task))")
    TaskSummaryDto toSummaryDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "developer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Task task, TaskRequestDto requestDto);

    default boolean isOverdue(Task task) {
        if (task.getDueDate() == null) return false;
        return task.getDueDate().isBefore(LocalDateTime.now()) &&
                task.getStatus() != TaskStatus.DONE;
    }

    default long getDaysUntilDue(Task task) {
        if (task.getDueDate() == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (task.getDueDate().isBefore(now)) {
            return -ChronoUnit.DAYS.between(task.getDueDate(), now);
        }
        return ChronoUnit.DAYS.between(now, task.getDueDate());
    }
}
