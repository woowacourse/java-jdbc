package org.springframework.jdbc.core.error.exception;

import java.sql.SQLException;

public class ColumnSqlRuntimeException extends SqlRuntimeException {

    private ColumnSqlRuntimeException(final SQLException exception) {
        super(exception);
    }

    public static class ColumnNotFoundException extends ColumnSqlRuntimeException {

        public ColumnNotFoundException(final SQLException exception) {

            super(exception);
        }
    }

    public static class ColumnCountDoestNotMatchException extends ColumnSqlRuntimeException {

        public ColumnCountDoestNotMatchException(final SQLException exception) {

            super(exception);
        }
    }
}
