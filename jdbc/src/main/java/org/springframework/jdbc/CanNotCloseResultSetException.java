package org.springframework.jdbc;

public class CanNotCloseResultSetException extends RuntimeException {

    public CanNotCloseResultSetException(String msg) {
        super(msg);
    }
}
