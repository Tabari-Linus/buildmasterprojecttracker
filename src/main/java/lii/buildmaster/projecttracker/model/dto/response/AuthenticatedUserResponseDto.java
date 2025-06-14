package lii.buildmaster.projecttracker.model.dto.response;

public record AuthenticatedUserDto(
        Long id,
        String username,
        String email,
        DeveloperResponseDto developer
) {}
