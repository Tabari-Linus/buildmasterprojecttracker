package lii.buildmaster.projecttracker.model.dto.response;

public record ApiResponse<T>(boolean success, T data, String message) {}
