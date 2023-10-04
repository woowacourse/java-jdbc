package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementExecuteStrategy<T> {

    T strategy(final PreparedStatement pstm) throws SQLException;
}
