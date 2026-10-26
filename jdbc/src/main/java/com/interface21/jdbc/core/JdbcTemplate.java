package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    public void update(
            String sql,
            Object... params
    ) {
        try (Connection connection = dataSource.getConnection()) {
            update(connection, sql, params);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    public void update(Connection connection, String sql, Object... params) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(params, statement);
            log.debug("query : {}", sql);

            statement.executeUpdate();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    public <T> Optional<T> selectOne(
            String sql,
            RowMapper<T> rowMapper,
            Object... params
    ) {
        try (Connection connection = dataSource.getConnection()) {
            return selectOne(connection, sql, rowMapper, params);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    public <T> Optional<T> selectOne(
            Connection connection,
            String sql,
            RowMapper<T> rowMapper,
            Object... params
    ) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(params, statement);
            log.debug("query : {}", sql);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                T result = Objects.requireNonNull(rowMapper.mapRow(resultSet));
                if (resultSet.next()) {
                    throw new DataAccessException("Expected at most one row, but found more than one");
                }
                return Optional.of(result);
            }
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    public <T> List<T> selectMany(
            String sql,
            RowMapper<T> rowMapper,
            Object... params
    ) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setParams(params, statement);
            log.debug("query : {}", sql);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(Objects.requireNonNull(rowMapper.mapRow(resultSet)));
                }
                return Collections.unmodifiableList(results);
            }
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    private void setParams(
            Object[] params,
            PreparedStatement statement
    ) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
