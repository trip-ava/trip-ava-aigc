package group.rxcloud.ava.aigc.service;

public class AIGCServiceException extends RuntimeException {

    public AIGCServiceException(String message) {
        super(message);
    }

    public AIGCServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
