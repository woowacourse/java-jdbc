package org.springframework.jdbc.core.error.exception;

import java.sql.SQLException;

public class MethodNotAllowedSqlRuntimeException extends SqlRuntimeException {

    private MethodNotAllowedSqlRuntimeException(final SQLException exception) {
        super(exception);
    }

    public static class ExecuteUpdateSqlRuntimeException extends MethodNotAllowedSqlRuntimeException {

        public ExecuteUpdateSqlRuntimeException(final SQLException exception) {
            super(exception);
        }
    }

    public static class ExecuteQuerySqlRuntimeException extends MethodNotAllowedSqlRuntimeException {

        public ExecuteQuerySqlRuntimeException(final SQLException exception) {
            super(exception);
        }
    }
}
