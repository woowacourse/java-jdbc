package org.springframework.jdbc.core;

public class UpdateFailedException extends RuntimeException {

    public UpdateFailedException(Throwable cause) {
        super(cause);
    }
}
