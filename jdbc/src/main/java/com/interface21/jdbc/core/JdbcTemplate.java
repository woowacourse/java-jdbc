package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        return executeQuery(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return executeQuery(sql, (pstmt) -> getSingleResult(rowMapper, pstmt), params);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return executeQuery(sql, (pstmt) -> getMultipleResult(rowMapper, pstmt), params);
    }

    private <T> T executeQuery(final String sql, final PreparedStatementExecutor<T> executor, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            setParams(pstmt, params);

            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T getSingleResult(
            final RowMapper<T> rowMapper,
            final PreparedStatement pstmt
    ) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        }
    }

    private <T> List<T> getMultipleResult(
            final RowMapper<T> rowMapper,
            final PreparedStatement pstmt
    ) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            final List<T> result = new ArrayList<>();
            while (rs.next()) {
                final T t = rowMapper.mapRow(rs);
                result.add(t);
            }
            return result;
        }
    }

    private void setParams(
            final PreparedStatement pstmt,
            final Object[] params
    ) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            pstmt.setObject(i + 1, param);
        }
    }
}
