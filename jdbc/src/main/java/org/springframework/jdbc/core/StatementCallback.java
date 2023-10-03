package org.springframework.jdbc.core;

import java.sql.SQLException;
import java.sql.Statement;

public interface StatementCallback<T> {

    T doInStatement(final String sql, final Statement statement) throws SQLException;
}
