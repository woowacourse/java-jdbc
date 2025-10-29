package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(final Connection connection, final String sql, final Object... params) {
        execute(connection, sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(final Connection connection, final String sql, RowMapper<T> mapper,
                             final Object... params) {
        return execute(connection, sql, ps -> {
            try (ResultSet resultSet = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(mapper.mapRow(resultSet));
                }

                return result;
            }
        }, params);
    }

    public <T> T queryForObject(final Connection connection, final String sql, RowMapper<T> mapper,
                                final Object... params) {
        List<T> result = query(connection, sql, mapper, params);
        if (result.size() != 1) {
            throw new IllegalStateException(
                    "Expected single row, but got " + result.size() + " rows"
            );
        }
        return result.getFirst();
    }

    private <R> R execute(final Connection connection, String sql, PreparedStatementSetter<R> action,
                          Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindParams(pstmt, params);

            return action.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void bindParams(final PreparedStatement pstmt, final Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        int index = 1;
        for (Object value : params) {
            pstmt.setObject(index++, value);
        }
    }

    public long updateAndReturnKey(final Connection connection, final String sql, final Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            log.debug("query : {}", sql);
            bindParams(pstmt, params);

            int updated = pstmt.executeUpdate();
            if (updated != 1) {
                throw new DataAccessException("Expected 1 row to be inserted, but updated=" + updated);
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new DataAccessException("No generated key returned from database");
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
