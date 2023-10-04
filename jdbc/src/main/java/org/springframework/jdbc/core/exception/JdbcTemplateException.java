package org.springframework.jdbc.core.exception;

public class JdbcTemplateException extends RuntimeException{

    public JdbcTemplateException(final Throwable cause) {
        super(cause);
    }
}
