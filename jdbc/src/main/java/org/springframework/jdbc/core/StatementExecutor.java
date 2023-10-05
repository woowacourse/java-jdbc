package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface StatementExecutor<T> {

    <T> List<T> execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException;
}
