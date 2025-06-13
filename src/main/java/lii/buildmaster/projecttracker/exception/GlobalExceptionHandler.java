package lii.buildmaster.projecttracker.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lii.buildmaster.projecttracker.model.dto.response.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleTaskNotFound(TaskNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("Task not found: " + ex.getTaskId(), "Not Found", HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProjectNotFoundException(ProjectNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("Project not found" + ex.getProjectId(), "Not Found", HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DeveloperNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleDeveloperNotFound(DeveloperNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("Task not found: " + ex.getDeveloperId(), "Not Found", HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("User with id: " + ex.getUserId() +" not found", "Not Found", HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto(ex.getMessage(), "Bad Request", HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("Invalid email or password", "Unauthorized", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUsernameNotFound(UsernameNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("User not found with the provided credentials", "Unauthorized", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponseDto> handleDisabledException(DisabledException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("Your account has been disabled. Please contact support.", "Forbidden", HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponseDto> handleLockedException(LockedException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("Your account has been locked. Please contact support.", "Forbidden", HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex) {
        logger.error("Authentication error: ", ex);
        return new ResponseEntity<>(
                new ErrorResponseDto("Authentication failed: " + ex.getMessage(), "Unauthorized", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto("Access denied. You don't have permission to access this resource.", "Forbidden", HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorizedException(UnauthorizedException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto(ex.getMessage(), "Unauthorized", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto(ex.getMessage(), "Forbidden", HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(ResourceAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceAccessDeniedException(ResourceAccessDeniedException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto(ex.getMessage(), "Access Denied", HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handleOAuth2AuthenticationException(OAuth2AuthenticationProcessingException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto(ex.getMessage(), "OAuth2 Authentication Failed", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(
                new ErrorResponseDto(ex.getMessage(), "Bad Request", HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                new ErrorResponseDto("Validation failed: " + errorMessage, "Bad Request", HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                new ErrorResponseDto("Validation failed: " + errorMessage, "Bad Request", HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

        return new ResponseEntity<>(
                new ErrorResponseDto(message, "Bad Request", HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());

        return new ResponseEntity<>(
                new ErrorResponseDto(message, "Not Found", HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("Method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));

        return new ResponseEntity<>(
                new ErrorResponseDto(message, "Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED.value()),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime exception occurred at path: " + request.getDescription(false), ex);

        // Provide more specific error messages for common runtime exceptions
        String message = "An unexpected error occurred";

        if (ex.getMessage() != null && ex.getMessage().contains("Role is not found")) {
            message = "System configuration error: Required role not found. Please contact support.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("duplicate key")) {
            message = "A record with the same unique identifier already exists.";
        } else if (ex.getMessage() != null) {
            message = "Error: " + ex.getMessage();
        }

        return new ResponseEntity<>(
                new ErrorResponseDto(message, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred at path: " + request.getDescription(false), ex);

        String message = "An unexpected error occurred";
        return new ResponseEntity<>(
                new ErrorResponseDto(message,
                        "Internal Server Error",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
