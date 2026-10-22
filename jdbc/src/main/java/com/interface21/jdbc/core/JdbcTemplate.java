package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 가변 인자는 SQL의 ? 순서대로 바인딩한다. SQL NULL 하나는 {@code (Object) null}로 전달한다.
 */
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public int update(final String sql, final Object... args) {
        return update(sql, createPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                            final RowMapper<T> mapper) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            try (final var resultSet = statement.executeQuery()) {
                final var results = new ArrayList<T>();
                while (resultSet.next()) {
                    results.add(mapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Object... args) {
        return query(sql, createPreparedStatementSetter(args), mapper);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, final Object... args) {
        return queryForObject(sql, createPreparedStatementSetter(args), mapper);
    }

    /**
     * 조회 결과가 없으면 null, 여러 행이면 첫 번째 행을 반환한다.
     */
    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter,
                               final RowMapper<T> mapper) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            try (final var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapper.mapRow(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... args) {
        Objects.requireNonNull(args, "args must not be null");
        return statement -> {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        };
    }
}
