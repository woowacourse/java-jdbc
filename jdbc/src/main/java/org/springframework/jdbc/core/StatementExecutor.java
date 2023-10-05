package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementExecutor<T> {

    <T> Object execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException;
}
