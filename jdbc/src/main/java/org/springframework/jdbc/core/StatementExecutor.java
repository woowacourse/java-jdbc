package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface StatementExecutor<T> {
    T execute(final ResultSet rs) throws SQLException;

}
