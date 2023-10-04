package org.springframework.exception;

public class UndeclaredThrowableException extends RuntimeException {

    public UndeclaredThrowableException(final Throwable cause) {
        super(cause);
    }

}
