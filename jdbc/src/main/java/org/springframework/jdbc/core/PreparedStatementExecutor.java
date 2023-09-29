package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementExecutor<T> {

    T fetchData(PreparedStatement statement) throws SQLException;
}
