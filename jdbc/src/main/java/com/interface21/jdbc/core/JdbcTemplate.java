package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public int update(String sql, Object... args) {
        return execute(sql, argumentsSetter(args), PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, argumentsSetter(args), pstmt -> mapRows(pstmt, rowMapper));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    private <T> T execute(String sql, PreparedStatementSetter setter, PreparedStatementCallback<T> action) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        T result;
        try {
            result = execute(conn, sql, setter, action);
        } catch (RuntimeException | Error e) {
            release(conn, e);
            throw e;
        }
        DataSourceUtils.releaseConnection(conn, dataSource);
        return result;
    }

    private void release(Connection conn, Throwable cause) {
        try {
            DataSourceUtils.releaseConnection(conn, dataSource);
        } catch (RuntimeException e) {
            cause.addSuppressed(e);
        }
    }

    private <T> T execute(Connection conn, String sql, PreparedStatementSetter setter, PreparedStatementCallback<T> action) {
        try (PreparedStatement pstmt = getPreparedStatement(conn, sql)) {
            setter.setValues(pstmt);
            return action.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    private PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
        log.debug("query : {}", sql);
        return conn.prepareStatement(sql);
    }

    private PreparedStatementSetter argumentsSetter(Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    private <T> List<T> mapRows(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }
}
