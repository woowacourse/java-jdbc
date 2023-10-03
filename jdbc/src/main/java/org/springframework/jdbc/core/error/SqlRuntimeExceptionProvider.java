package org.springframework.jdbc.core.error;

import java.sql.SQLException;
import org.springframework.jdbc.core.error.exception.SqlRuntimeException;

@FunctionalInterface
public interface SqlRuntimeExceptionProvider {

    SqlRuntimeException provide(final SQLException exception);
}
