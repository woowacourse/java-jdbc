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
import org.springframework.jdbc.core.exception.DataSourceNotFoundException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(params, preparedStatement);

            try (final var resultSet = preparedStatement.executeQuery()) {
                log.info("JDBC QUERY SQL = {}", sql);
                return getResults(rowMapper, resultSet);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error(e.getMessage(), e);
            throw new DataSourceNotFoundException(e.getMessage(), e);
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

    public <T> T executeQueryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(params, preparedStatement);

            try (final var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    log.info("JDBC QUERY_FOR_OBJECT SQL = {}", sql);
                    return rowMapper.mapRow(resultSet);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error(e.getMessage(), e);
            throw new DataSourceNotFoundException(e.getMessage(), e);
        }
        return null;
    }

    public void update(final String sql, final Object... params) {
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            setParameters(params, preparedStatement);
            preparedStatement.executeUpdate();
            log.info("JDBC UPDATE SQL = {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error(e.getMessage(), e);
            throw new DataSourceNotFoundException(e.getMessage(), e);
        }
    }

    private Connection getConnection() throws SQLException {
        return requireNonNull(dataSource, "DataSource가 null입니다.").getConnection();
    }

    private void setParameters(final Object[] params, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
