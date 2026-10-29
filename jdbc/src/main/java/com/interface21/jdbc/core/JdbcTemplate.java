package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }

                return results;
            }
        }, params);
    }

    private <R> R execute(
            final String sql,
            final JdbcOperation<R> operation,
            final Object... params
    ) {
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            return execute(conn, sql, operation, params);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private <R> R execute(
            final Connection conn,
            final String sql,
            final JdbcOperation<R> operation,
            final Object... params
    ) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return operation.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    /**
     * Returns the mapped value of exactly one row.
     *
     * @throws IncorrectResultSizeDataAccessException if the query returns zero or multiple rows
     */
    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        List<T> results = query(sql, rowMapper, params);

        if (results.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }

        return results.get(0);
    }

    @FunctionalInterface
    private interface JdbcOperation<R> {
        R execute(PreparedStatement pstmt) throws SQLException;
    }
}
