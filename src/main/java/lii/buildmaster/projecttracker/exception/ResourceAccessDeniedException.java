package lii.buildmaster.projecttracker.exception;

public class ResourceAccessDeniedException extends RuntimeException {
  public ResourceAccessDeniedException(String message) {
    super(message);
  }
}
