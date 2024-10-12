package com.interface21.jdbc.core;

public class JdbcTemplateException extends RuntimeException {

    public JdbcTemplateException(Throwable cause) {
        super(cause);
    }

    public JdbcTemplateException(String message) {
        super(message);
    }
}
