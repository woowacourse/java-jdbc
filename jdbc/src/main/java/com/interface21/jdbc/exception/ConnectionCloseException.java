package com.interface21.jdbc.exception;

public class ConnectionCloseException extends JdbcException {
    public ConnectionCloseException(Throwable cause, String sql) {
        super("Failed to close connection", cause, sql);
    }
}
