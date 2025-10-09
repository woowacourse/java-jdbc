package com.interface21.jdbc.exception;

public class QueryExecutionException extends JdbcException {
    public QueryExecutionException(Throwable cause, String sql) {
        super("Failed to execute query", cause, sql);
    }
}
