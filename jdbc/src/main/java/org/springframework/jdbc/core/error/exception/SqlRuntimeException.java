package org.springframework.jdbc.core.error.exception;

import java.sql.SQLException;
import org.springframework.jdbc.core.error.SqlExceptionType;

public class SqlRuntimeException extends RuntimeException {

    private final SqlExceptionType exceptionType;
    private final String localizedMessage;

    public SqlRuntimeException(final SQLException exception) {
        this.exceptionType = SqlExceptionType.findByException(exception);
        this.localizedMessage = exception.getLocalizedMessage();
    }

    public SqlExceptionType getExceptionType() {
        return exceptionType;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }
}
