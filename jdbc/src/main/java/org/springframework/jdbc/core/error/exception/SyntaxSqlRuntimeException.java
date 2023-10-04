package org.springframework.jdbc.core.error.exception;

import java.sql.SQLException;

public class SyntaxSqlRuntimeException extends SqlRuntimeException {

    public SyntaxSqlRuntimeException(final SQLException exception) {
        super(exception);
    }
}
