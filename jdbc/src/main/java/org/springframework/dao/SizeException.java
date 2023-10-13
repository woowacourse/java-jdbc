package org.springframework.dao;

public class SizeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SizeException() {
        super();
    }

    public SizeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SizeException(String message) {
        super(message);
    }

    public SizeException(Throwable cause) {
        super(cause);
    }
}
