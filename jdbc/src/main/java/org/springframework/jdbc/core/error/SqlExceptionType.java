package org.springframework.jdbc.core.error;

import java.sql.SQLException;
import java.util.Arrays;
import org.springframework.jdbc.core.error.exception.ColumnSqlRuntimeException;
import org.springframework.jdbc.core.error.exception.DataConversionSqlRuntimeException;
import org.springframework.jdbc.core.error.exception.MethodNotAllowedSqlRuntimeException.ExecuteQuerySqlRuntimeException;
import org.springframework.jdbc.core.error.exception.MethodNotAllowedSqlRuntimeException.ExecuteUpdateSqlRuntimeException;
import org.springframework.jdbc.core.error.exception.SqlRuntimeException;
import org.springframework.jdbc.core.error.exception.SyntaxSqlRuntimeException;

public enum SqlExceptionType {
    METHOD_NOT_ALLOWED_EXECUTE_UPDATE(90001, ExecuteUpdateSqlRuntimeException::new),
    METHOD_NOT_ALLOWED_EXECUTE_QUERY(90002, ExecuteQuerySqlRuntimeException::new),
    SYNTAX_ERROR(42001, SyntaxSqlRuntimeException::new),
    TABLE_NOT_FOUND(42102, TableNotFoundSqlRuntimeException::new),
    COLUMN_NOT_FOUND(42122, ColumnSqlRuntimeException.ColumnNotFoundException::new),
    COLUMN_COUNT_DOES_NOT_MATCH(21002, ColumnSqlRuntimeException.ColumnCountDoestNotMatchException::new),
    DATA_CAN_NOT_CONVERT(22018, DataConversionSqlRuntimeException::new),
    UNHANDLABLE(0, SqlRuntimeException::new);

    private final int code;
    private final SqlRuntimeExceptionProvider exceptionProvider;

    SqlExceptionType(final int code, final SqlRuntimeExceptionProvider exceptionProvider) {
        this.code = code;
        this.exceptionProvider = exceptionProvider;
    }

    public static SqlExceptionType findByException(final SQLException exception) {
        return Arrays.stream(values())
            .filter(error -> error.code == exception.getErrorCode())
            .findFirst()
            .orElse(UNHANDLABLE);
    }

    public SqlRuntimeExceptionProvider getExceptionProvider() {
        return exceptionProvider;
    }
}
