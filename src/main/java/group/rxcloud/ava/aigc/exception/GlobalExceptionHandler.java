package group.rxcloud.ava.aigc.exception;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * Global Exception Handler for AVA AIGC
 * Handles all exceptions and returns standardized error responses
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Error response DTO
     */
    @Data
    public static class ErrorResponse {
        private String error;
        private String message;
        private String path;
        private int status;
        private LocalDateTime timestamp;
    }

    /**
     * Handle AIGC service exceptions
     */
    @ExceptionHandler(group.rxcloud.ava.aigc.service.AIGCServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleAIGCServiceException(
            group.rxcloud.ava.aigc.service.AIGCServiceException ex,
            HttpServletRequest request) {
        log.error("AIGC Service error occurred: {}", ex.getErrorCode(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setError(ex.getErrorCode().getCode());
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse error = new ErrorResponse();
        error.setError("Internal Server Error");
        error.setMessage("An unexpected error occurred. Please try again later.");
        error.setPath(request.getRequestURI());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        log.error("Runtime error occurred", ex);

        ErrorResponse error = new ErrorResponse();
        error.setError("Bad Request");
        error.setMessage(ex.getMessage() != null ? ex.getMessage() : "Invalid request");
        error.setPath(request.getRequestURI());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        log.error("File upload size exceeded", ex);

        ErrorResponse error = new ErrorResponse();
        error.setError("Payload Too Large");
        error.setMessage("The uploaded file is too large. Please upload a smaller file.");
        error.setPath(request.getRequestURI());
        error.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        log.error("Invalid argument", ex);

        ErrorResponse error = new ErrorResponse();
        error.setError("Invalid Argument");
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle null pointer exceptions
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleNullPointer(
            NullPointerException ex,
            HttpServletRequest request) {
        log.error("Null pointer exception", ex);

        ErrorResponse error = new ErrorResponse();
        error.setError("Internal Error");
        error.setMessage("A processing error occurred. Please try again.");
        error.setPath(request.getRequestURI());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
