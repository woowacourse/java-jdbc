package com.interface21.jdbc.core;

import com.interface21.jdbc.exception.DataAccessException;
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
        return execute(connection, sql, ps -> {
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    T result = mapper.mapRow(resultSet);
                    if (resultSet.next()) {
                        throw new IllegalStateException("Expected single row, but got multiple rows");

                    }
                    return result;
                }
                throw new IllegalStateException("Expected single row, but gone none");
            }
        }, params);
    }

    private <R> R execute(final Connection connection, String sql, PreparedStatementSetter<R> action,
                          Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindParams(pstmt, params);

            return action.setValues(pstmt);
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
}
