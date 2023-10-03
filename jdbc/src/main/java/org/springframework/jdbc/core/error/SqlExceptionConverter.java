package org.springframework.jdbc.core.error;

import java.sql.SQLException;
import org.springframework.jdbc.core.error.exception.SqlRuntimeException;

public class SqlExceptionConverter {

    private SqlExceptionConverter() {

    }

    public static SqlRuntimeException convert(final SQLException sqlException) {
        final SqlExceptionType sqlExceptionType = SqlExceptionType.findByException(sqlException);
        final SqlRuntimeExceptionProvider exceptionProvider = sqlExceptionType.getExecution();
        return exceptionProvider.provide(sqlException);
    }
}
