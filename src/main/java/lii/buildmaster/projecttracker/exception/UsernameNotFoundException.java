package lii.buildmaster.projecttracker.exception;

import lombok.Getter;

@Getter
public class UsernameNotFoundException extends RuntimeException {
    private final Long userId;

    public UsernameNotFoundException(Long userId) {
        super("Develops with ID " + userId + " not found.");
        this.userId = userId;
    }
}
