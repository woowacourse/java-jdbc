package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.NonUniqueResultException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        execute(sql, pstmt -> {
                    setParams(params, pstmt);
                    return pstmt.executeUpdate();
                });
    }

    public void update(String sql, PreparedStatementSetter setter) {
        execute(sql, pstmt -> {
            setter.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public void update(String sql, PreparedStatementSetter setter, Connection connection) {
        execute(sql, pstmt -> {
            setter.setValues(pstmt);
            return pstmt.executeUpdate();
        }, connection);
    }

    public <T> List<T> find(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> {
            setParams(params, pstmt);
            return executeQuery(pstmt, rowMapper);
        });
    }

    public <T> List<T> find(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            setter.setValues(pstmt);
            return executeQuery(pstmt, rowMapper);
        });
    }

    public <T> Optional<T> findOne(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> {
            setParams(params, pstmt);
            return executeQueryOne(pstmt, rowMapper);
        });
    }

    public <T> Optional<T> findOne(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            setter.setValues(pstmt);
            return executeQueryOne(pstmt, rowMapper);
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return callback.run(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback, Connection connection) {
        try (
                PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return callback.run(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> objects = new ArrayList<>();
            while (rs.next()) {
                objects.add(rowMapper.map(rs));
            }
            return objects;
        }
    }

    private <T> Optional<T> executeQueryOne(final PreparedStatement pstmt, final RowMapper<T> rowMapper)
            throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            T result = rowMapper.map(rs);
            validateUniqueResult(rs);
            return Optional.of(result);
        }
    }

    private static void validateUniqueResult(final ResultSet rs) throws SQLException {
        if (rs.next()) {
            throw new NonUniqueResultException("Multiple results found");
        }
    }

    private void setParams(final Object[] params, final PreparedStatement pstmt) throws SQLException {
        for (int i = 1; i <= params.length; i++) {
            pstmt.setObject(i, params[i - 1]);
        }
    }
}
