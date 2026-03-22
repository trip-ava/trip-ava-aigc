package group.rxcloud.ava.aigc.service;

import lombok.Getter;

/**
 * Custom exception for AIGC Service errors
 * Includes error codes for better error handling
 */
@Getter
public class AIGCServiceException extends RuntimeException {

    private final ErrorCode errorCode;

    public enum ErrorCode {
        CONFIGURATION_ERROR("AIGC001", "Configuration error"),
        API_CALL_FAILED("AIGC002", "External API call failed"),
        INVALID_INPUT("AIGC003", "Invalid input provided"),
        PROCESSING_ERROR("AIGC004", "Processing error"),
        TIMEOUT_ERROR("AIGC005", "Request timeout"),
        UNKNOWN_ERROR("AIGC999", "Unknown error");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    public AIGCServiceException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNKNOWN_ERROR;
    }

    public AIGCServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.UNKNOWN_ERROR;
    }

    public AIGCServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AIGCServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
