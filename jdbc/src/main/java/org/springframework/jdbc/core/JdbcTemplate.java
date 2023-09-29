package org.springframework.jdbc.core;

import static java.util.Objects.requireNonNull;

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

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final var connection = requireNonNull(dataSource, "dataSource가 null입니다.").getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(params, preparedStatement);

            try (final var resultSet = preparedStatement.executeQuery()) {
                return getResults(rowMapper, resultSet);
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            final T result = rowMapper.mapRow(resultSet);
            results.add(result);
        }
        return results;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final var connection = requireNonNull(dataSource).getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(params, preparedStatement);

            try (final var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public void update(final String sql, final Object... params) {
        try (final var connection = requireNonNull(dataSource, "dataSource가 null입니다.").getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(params, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(final Object[] params, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
