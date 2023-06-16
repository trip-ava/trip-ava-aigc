package group.rxcloud.ava.aigc.ai;

public class AIGCException extends RuntimeException {

    public AIGCException() {
        super();
    }

    public AIGCException(String message) {
        super(message);
    }

    public AIGCException(String message, Throwable cause) {
        super(message, cause);
    }

    public AIGCException(Throwable cause) {
        super(cause);
    }

    protected AIGCException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
