package lii.buildmaster.projecttracker.mapper;

import lii.buildmaster.projecttracker.model.dto.request.DeveloperRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.DeveloperResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.DeveloperSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Developer;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DeveloperMapper {

    Developer toEntity(DeveloperRequestDto requestDto);

    @Mapping(target = "totalTaskCount", expression = "java(getTotalTaskCount(developer))")
    @Mapping(target = "activeTaskCount", expression = "java(getActiveTaskCount(developer))")
    @Mapping(target = "completedTaskCount", expression = "java(getCompletedTaskCount(developer))")
    DeveloperResponseDto toResponseDto(Developer developer);

    @Mapping(target = "activeTaskCount", expression = "java(getActiveTaskCount(developer))")
    DeveloperSummaryDto toSummaryDto(Developer developer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedTasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Developer developer, DeveloperRequestDto requestDto);

    default long getTotalTaskCount(Developer developer) {
        return developer.getAssignedTasks() != null ? developer.getAssignedTasks().size() : 0;
    }

    default long getActiveTaskCount(Developer developer) {
        if (developer.getAssignedTasks() == null) return 0;
        return developer.getAssignedTasks().stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .count();
    }

    default long getCompletedTaskCount(Developer developer) {
        if (developer.getAssignedTasks() == null) return 0;
        return developer.getAssignedTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();
    }
}
