package org.springframework.jdbc.core;

public class SelectFailedException extends RuntimeException {

    public SelectFailedException(Throwable e) {
        super(e);
    }
}
