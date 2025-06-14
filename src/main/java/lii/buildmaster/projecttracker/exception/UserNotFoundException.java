package lii.buildmaster.projecttracker.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("Develops with ID " + userId + " not found.");
        this.userId = userId;
    }
}
