package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementExecutor<T> {
    T execute(final PreparedStatement preparedStatement) throws SQLException;
}
