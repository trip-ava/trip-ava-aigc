package group.rxcloud.ava.aigc.exception;

import group.rxcloud.ava.aigc.service.AIGCServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleAIGCServiceException() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        AIGCServiceException exception = new AIGCServiceException(
                AIGCServiceException.ErrorCode.API_CALL_FAILED,
                "API call failed"
        );

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleAIGCServiceException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AIGC002", response.getBody().getError());
        assertEquals("API call failed", response.getBody().getMessage());
    }

    @Test
    void testHandleRuntimeException() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        RuntimeException exception = new RuntimeException("Something went wrong");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleRuntimeException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Something went wrong", response.getBody().getMessage());
    }

    @Test
    void testHandleMaxUploadSizeExceeded() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1000);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleMaxUploadSizeExceeded(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Payload Too Large", response.getBody().getError());
    }

    @Test
    void testHandleIllegalArgument() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgument(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Argument", response.getBody().getError());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void testHandleNullPointer() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        NullPointerException exception = new NullPointerException();

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleNullPointer(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Error", response.getBody().getError());
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        HttpServletRequest request = new MockHttpServletRequest();
        Exception exception = new Exception("Generic error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleGenericException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
    }
}
