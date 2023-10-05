package org.springframework.dao;

public class JdbcException extends RuntimeException {

    public JdbcException(Throwable cause) {
        super(cause);
    }
}
