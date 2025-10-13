package com.interface21.jdbc.core;

import java.sql.Connection;
import java.util.List;

public interface JdbcOperations {

    int update(String sql, Object... args);

    int update(final Connection conn, final String sql, final Object... args);

    int update(String sql, PreparedStatementSetter pss);

    int update(final Connection conn, final String sql, final PreparedStatementSetter pss);

    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args);

    <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper);

    <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args);

    <T> T queryForObject(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper);

    <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse);
}
