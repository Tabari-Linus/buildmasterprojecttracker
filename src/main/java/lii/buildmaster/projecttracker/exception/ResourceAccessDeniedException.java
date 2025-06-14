package lii.buildmaster.projecttracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
@Getter
public class ResourceAccessDeniedException extends RuntimeException {

    private final String resource;
    private final Long resourceId;

    public ResourceAccessDeniedException(String resource, Long resourceId) {
        super(String.format("Access denied to %s with id: %d", resource, resourceId));
        this.resource = resource;
        this.resourceId = resourceId;
    }
}
