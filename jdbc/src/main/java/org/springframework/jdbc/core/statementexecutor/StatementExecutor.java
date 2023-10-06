package org.springframework.jdbc.core.statementexecutor;

import org.springframework.jdbc.core.rowmapper.RowMapper;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface StatementExecutor<T> {

    <T> Object execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper);
}
