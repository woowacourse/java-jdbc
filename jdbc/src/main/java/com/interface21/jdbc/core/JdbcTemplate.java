package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        final var results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new DataAccessException("result size doesn't match: " + results.size());
        }
        return results.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return queryInternal(conn, sql, rowMapper, args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public <T> List<T> query(String sql, Connection connection, RowMapper<T> rowMapper, Object... args) {
        return executeWithConnection(connection, conn -> queryInternal(conn, sql, rowMapper, args));
    }

    public int update(String sql, Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return updateInternal(conn, sql, args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public int update(String sql, Connection connection, Object... args) {
        return executeWithConnection(connection, conn -> updateInternal(conn, sql, args));
    }

    private void setPreparedStatementParams(final PreparedStatement pstmt, final Object... args) throws SQLException {
        final int parameterCount = pstmt.getParameterMetaData().getParameterCount();
        if (args.length != parameterCount) {
            throw new DataAccessException();
        }
        for (int i = 0; i < parameterCount; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> T execute(ConnectionCallback<T> callback) {
        try (var conn = dataSource.getConnection()) {
            return callback.doInConnection(conn);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeWithConnection(Connection connection, ConnectionCallback<T> callback) {
        try {
            return callback.doInConnection(connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> queryInternal(Connection conn, String sql, RowMapper<T> rowMapper, Object... args) throws SQLException {
        try (var pstmt = conn.prepareStatement(sql)) {
            setPreparedStatementParams(pstmt, args);
            try (var rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                return extractResults(rs, rowMapper);
            }
        }
    }

    private <T> List<T> extractResults(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }

    private int updateInternal(Connection conn, String sql, Object... args) throws SQLException {
        try (var pstmt = conn.prepareStatement(sql)) {
            setPreparedStatementParams(pstmt, args);
            return pstmt.executeUpdate();
        }
    }
}
