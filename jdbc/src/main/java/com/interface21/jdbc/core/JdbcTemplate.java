package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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

    public int update(String sql, Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, pstmt -> setParameters(pstmt, params));
    }

    public int update(Connection conn, String sql, Object... params) {
        return execute(conn, sql, PreparedStatement::executeUpdate, pstmt -> setParameters(pstmt, params));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> readRows(pstmt, rowMapper), pstmt -> setParameters(pstmt, params));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return singleResult(query(sql, rowMapper, params));
    }

    public <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = execute(conn, sql, pstmt -> readRows(pstmt, rowMapper),
                pstmt -> setParameters(pstmt, params));
        return singleResult(results);
    }

    private static <T> List<T> readRows(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }

    private static <T> T singleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.getFirst();
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback, PreparedStatementSetter pstmtSetter) {
        try (Connection conn = dataSource.getConnection()) {
            return execute(conn, sql, callback, pstmtSetter);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Connection conn, String sql, PreparedStatementCallback<T> callback,
                          PreparedStatementSetter pstmtSetter) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pstmtSetter.setValues(pstmt);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private static void setParameters(PreparedStatement pstmt, Object[] params) throws SQLException {
        for (int i = 1; i <= params.length; i++) {
            pstmt.setObject(i, params[i - 1]);
        }
    }
}
