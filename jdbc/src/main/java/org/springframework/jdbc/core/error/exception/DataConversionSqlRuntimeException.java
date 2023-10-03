package org.springframework.jdbc.core.error.exception;

import java.sql.SQLException;

public class DataConversionSqlRuntimeException extends SqlRuntimeException {

    public DataConversionSqlRuntimeException(final SQLException exception) {
        super(exception);
    }
}
