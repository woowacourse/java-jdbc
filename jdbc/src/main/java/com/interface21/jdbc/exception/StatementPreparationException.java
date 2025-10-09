package com.interface21.jdbc.exception;

public class StatementPreparationException extends JdbcException {
    public StatementPreparationException(Throwable cause, String sql) {
        super("Failed to prepare statement", cause, sql);
    }
}
