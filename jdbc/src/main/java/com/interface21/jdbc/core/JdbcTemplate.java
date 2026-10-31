package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return execute(sql, parameters, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, parameters, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                final List<T> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(rowMapper.mapRow(resultSet));
                }
                return rows;
            }
        });
    }

    /**
     * 결과가 없으면 null, 여러 행이면 첫 번째 결과를 반환한다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final var rows = query(sql, rowMapper, parameters);
        if (rows.isEmpty()) {
            return null;
        }
        return rows.getFirst();
    }

    private <T> T execute(final String sql, final Object[] parameters, final StatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);
            log.debug("query : {}", sql);
            return callback.execute(statement);
        } catch (SQLException exception) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", exception);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] parameters) throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
