package org.springframework.jdbc.core.error.exception;

import java.sql.SQLException;

public class TableNotFoundSqlRuntimeException extends SqlRuntimeException {

    public TableNotFoundSqlRuntimeException(final SQLException exception) {
        super(exception);
    }
}
