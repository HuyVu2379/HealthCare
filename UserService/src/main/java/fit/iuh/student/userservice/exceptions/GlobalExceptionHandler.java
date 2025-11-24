package fit.iuh.student.userservice.exceptions;

import fit.iuh.student.userservice.dtos.responses.ErrorEntityResponse;
import fit.iuh.student.userservice.dtos.responses.ErrorResponse;
import fit.iuh.student.userservice.exceptions.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse<String>> handleFileUploadException(FileUploadException ex, HttpServletRequest request) {
        return ErrorEntityResponse.error("Upload File Failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
    }

    @ExceptionHandler(FileDeleteException.class)
    public ResponseEntity<ErrorResponse<String>> handleFileDeleteException(FileDeleteException ex, HttpServletRequest request) {
        return ErrorEntityResponse.error("Delete File Failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse<String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return ErrorEntityResponse.error("File size exceeds the maximum limit of 10MB", HttpStatus.PAYLOAD_TOO_LARGE.value(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<String>> handleGlobalException(Exception ex, HttpServletRequest request) {
        return ErrorEntityResponse.error("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return ErrorEntityResponse.error("User not found: " + ex.getMessage(), HttpStatus.NOT_FOUND.value(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        errors.put("path", request.getRequestURI());
        return ErrorEntityResponse.error("Validation failed", HttpStatus.BAD_REQUEST.value(), errors);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse<String>> UnauthorizedException(UnauthorizedException exc, HttpServletRequest request) {
        return ErrorEntityResponse.error(exc.getMessage(), HttpStatus.UNAUTHORIZED.value(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse<String>> handleAccessDeniedException(AccessDeniedException exc, HttpServletRequest request) {
        return ErrorEntityResponse.error("you can't access to the resource!", HttpStatus.FORBIDDEN.value(), request.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleAccessDeniedException(NotFoundException exc, HttpServletRequest request) {
        return ErrorEntityResponse.error("Not found: " + exc.getMessage(), HttpStatus.NOT_FOUND.value(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse<String>> handleDuplicateUserException(DuplicateUserException ex, HttpServletRequest request) {
        return ErrorEntityResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value(), request.getRequestURI());
    }
}
