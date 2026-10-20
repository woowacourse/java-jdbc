package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SQL 실행, JDBC 자원 정리와 SQLException 변환을 담당한다.
 *
 * <p>가변 인자는 SQL의 ? 순서대로 바인딩하며, 인자가 없으면 생략한다.
 * SQL NULL 하나는 {@code (Object) null} 또는 {@code new Object[]{null}}로 전달한다.
 * 단순히 null을 전달하면 오버로드가 모호하거나 null 배열로 해석될 수 있다.
 * SQL, 콜백과 인자 배열 자체는 null일 수 없지만 배열 안의 null 요소는 허용한다.
 */
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    /**
     * 매개변수 바인딩 후 SQL을 실행하고 영향을 받은 행 수를 반환한다.
     */
    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    /**
     * 가변 인자를 순서대로 바인딩하고 영향을 받은 행 수를 반환한다.
     */
    public int update(final String sql, final Object... parameters) {
        return update(sql, createPreparedStatementSetter(parameters));
    }

    /**
     * 각 행을 변환한 목록을 반환한다. 조회 결과가 없으면 빈 목록을 반환한다.
     */
    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                            final RowMapper<T> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return execute(sql, setter, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                final var results = new ArrayList<T>();
                while (resultSet.next()) {
                    results.add(mapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    /**
     * 가변 인자를 바인딩하여 각 행을 변환한다. 조회 결과가 없으면 빈 목록을 반환한다.
     */
    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        return query(sql, createPreparedStatementSetter(parameters), mapper);
    }

    /**
     * 조회 결과가 없으면 null, 여러 행이면 첫 번째 행을 반환한다.
     */
    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter,
                               final RowMapper<T> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return execute(sql, setter, statement -> {
            try (final var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapper.mapRow(resultSet);
                }
                return null;
            }
        });
    }

    /**
     * 가변 인자를 바인딩하여 조회한다. 결과가 없으면 null, 여러 행이면 첫 번째 행을 반환한다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        return queryForObject(sql, createPreparedStatementSetter(parameters), mapper);
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(setter, "setter must not be null");
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.doInPreparedStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");
        return statement -> {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(PreparedStatement statement) throws SQLException;
    }
}
