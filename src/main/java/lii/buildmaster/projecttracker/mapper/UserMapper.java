package lii.buildmaster.projecttracker.mapper;

import lii.buildmaster.projecttracker.model.dto.response.UserResponseDto;
import lii.buildmaster.projecttracker.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "developer.id", target = "developerId")
    UserResponseDto toResponseDto(User user);
}
