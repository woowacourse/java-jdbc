package org.springframework.jdbc.core.error;

import java.sql.SQLException;
import org.springframework.jdbc.core.error.exception.SqlRuntimeException;

public class TableNotFoundSqlRuntimeException extends SqlRuntimeException {

    public TableNotFoundSqlRuntimeException(final SQLException exception) {
        super(exception);
    }
}
