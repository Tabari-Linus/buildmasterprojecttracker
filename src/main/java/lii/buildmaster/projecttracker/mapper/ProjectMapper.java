package lii.buildmaster.projecttracker.mapper;

import lii.buildmaster.projecttracker.model.dto.request.ProjectRequestDto;
import lii.buildmaster.projecttracker.model.dto.response.ProjectResponseDto;
import lii.buildmaster.projecttracker.model.dto.summary.ProjectSummaryDto;
import lii.buildmaster.projecttracker.model.entity.Project;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {


    Project toEntity(ProjectRequestDto requestDto);


    @Mapping(target = "taskCount", expression = "java(getTaskCount(project))")
    ProjectResponseDto toResponseDto(Project project);


    @Mapping(target = "taskCount", expression = "java(getTaskCount(project))")
    @Mapping(target = "completedTaskCount", expression = "java(getCompletedTaskCount(project))")
    ProjectSummaryDto toSummaryDto(Project project);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Project project, ProjectRequestDto requestDto);

    default long getTaskCount(Project project) {
        return project.getTasks() != null ? project.getTasks().size() : 0;
    }

    default long getCompletedTaskCount(Project project) {
        if (project.getTasks() == null) return 0;
        return project.getTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();
    }
}
