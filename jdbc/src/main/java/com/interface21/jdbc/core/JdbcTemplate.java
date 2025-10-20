package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public void update(Connection connection, String sql, Object... params) {
        execute(connection, sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, preStmt -> {
            try (ResultSet rs = preStmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        }, params);
    }

    public <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(connection, sql, preStmt -> {
            try (ResultSet rs = preStmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        }, params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            throw new DataAccessException("데이터가 존재하지 않습니다");
        }
        if (results.size() > 1) {
            throw new DataAccessException("한 개의 결과만을 반환해야 합니다: " + results.size());
        }
        return results.getFirst();
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(connection, sql, rowMapper, params);
        if (results.isEmpty()) {
            throw new DataAccessException("데이터가 존재하지 않습니다");
        }
        if (results.size() > 1) {
            throw new DataAccessException("한 개의 결과만을 반환해야 합니다: " + results.size());
        }
        return results.getFirst();
    }

    private <T> T execute(Connection connection, String sql, PreparedStatementCallback<T> callback, Object... params) {
        try (PreparedStatement preStmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(params, preStmt);
            return callback.doInPreparedStatement(preStmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preStmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            setParameters(params, preStmt);
            return callback.doInPreparedStatement(preStmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParameters(Object[] params, PreparedStatement preStmt) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preStmt.setObject(i + 1, params[i]);
        }
    }
}
