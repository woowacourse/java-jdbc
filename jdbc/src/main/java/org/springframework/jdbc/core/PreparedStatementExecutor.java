package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementExecutor<T> {

    T query(final PreparedStatement preparedStatement) throws SQLException;

}
