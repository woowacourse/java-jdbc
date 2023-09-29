package org.springframework.jdbc.core;

import java.sql.SQLException;
import java.sql.Statement;

public interface StatementCallback<T> {

    T doInStatement(final Statement statement) throws SQLException;

    String getSql();
}
