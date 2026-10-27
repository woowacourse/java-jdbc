package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 *
 * <p>Connection을 전달받는 메서드는 연결을 닫지 않는다. 연결을 연 호출자가 종료를 담당한다.
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

    public int update(final Connection connection, final String sql, final PreparedStatementSetter setter) {
        return execute(connection, sql, setter, PreparedStatement::executeUpdate);
    }

    public int update(final Connection connection, final String sql, final Object... parameters) {
        return update(connection, sql, createPreparedStatementSetter(parameters));
    }

    /**
     * 각 행을 변환한 목록을 반환한다. 조회 결과가 없으면 빈 목록을 반환한다.
     */
    public <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                            final RowMapper<T> mapper) {
        return execute(sql, setter, createQueryCallback(mapper));
    }

    /**
     * 가변 인자를 바인딩하여 각 행을 변환한다. 조회 결과가 없으면 빈 목록을 반환한다.
     */
    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        return query(sql, createPreparedStatementSetter(parameters), mapper);
    }

    public <T> List<T> query(final Connection connection, final String sql,
                            final PreparedStatementSetter setter, final RowMapper<T> mapper) {
        return execute(connection, sql, setter, createQueryCallback(mapper));
    }

    public <T> List<T> query(final Connection connection, final String sql,
                            final RowMapper<T> mapper, final Object... parameters) {
        return query(connection, sql, createPreparedStatementSetter(parameters), mapper);
    }

    /**
     * 조회 결과가 없으면 null, 한 행이면 변환한 객체를 반환한다. 두 행 이상이면 예외를 던진다.
     */
    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter,
                               final RowMapper<T> mapper) {
        return execute(sql, setter, createQueryForObjectCallback(mapper));
    }

    /**
     * 가변 인자를 바인딩하여 조회한다. 결과가 없으면 null, 한 행이면 객체를 반환하고 두 행 이상이면 예외를 던진다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, final Object... parameters) {
        return queryForObject(sql, createPreparedStatementSetter(parameters), mapper);
    }

    public <T> T queryForObject(final Connection connection, final String sql,
                               final PreparedStatementSetter setter, final RowMapper<T> mapper) {
        return execute(connection, sql, setter, createQueryForObjectCallback(mapper));
    }

    public <T> T queryForObject(final Connection connection, final String sql,
                               final RowMapper<T> mapper, final Object... parameters) {
        return queryForObject(connection, sql, createPreparedStatementSetter(parameters), mapper);
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter,
                          final PreparedStatementCallback<T> callback) {
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(setter, "setter must not be null");
        try {
            final var connection = DataSourceUtils.doGetConnection(dataSource);
            Throwable failure = null;
            try {
                return doExecute(connection, sql, setter, callback);
            } catch (SQLException | RuntimeException | Error e) {
                failure = e;
                throw e;
            } finally {
                DataSourceUtils.doReleaseConnection(connection, dataSource, failure);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private <T> T execute(final Connection connection, final String sql,
                          final PreparedStatementSetter setter, final PreparedStatementCallback<T> callback) {
        Objects.requireNonNull(connection, "connection must not be null");
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(setter, "setter must not be null");
        try {
            return doExecute(connection, sql, setter, callback);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private <T> T doExecute(final Connection connection, final String sql,
                            final PreparedStatementSetter setter, final PreparedStatementCallback<T> callback)
            throws SQLException {
        log.debug("query : {}", sql);
        try (final var statement = connection.prepareStatement(sql)) {
            setter.setValues(statement);
            return callback.doInPreparedStatement(statement);
        }
    }

    private <T> PreparedStatementCallback<List<T>> createQueryCallback(final RowMapper<T> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return createResultSetCallback(resultSet -> {
            final var results = new ArrayList<T>();
            while (resultSet.next()) {
                results.add(mapper.mapRow(resultSet));
            }
            return results;
        });
    }

    private <T> PreparedStatementCallback<T> createQueryForObjectCallback(final RowMapper<T> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return createResultSetCallback(resultSet -> {
            if (!resultSet.next()) {
                return null;
            }
            final var result = mapper.mapRow(resultSet);
            if (resultSet.next()) {
                throw new DataAccessException("Expected at most one row, but found two or more");
            }
            return result;
        });
    }

    private <T> PreparedStatementCallback<T> createResultSetCallback(final ResultSetExtractor<T> extractor) {
        return statement -> {
            try (final var resultSet = statement.executeQuery()) {
                return extractor.extractData(resultSet);
            }
        };
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

    @FunctionalInterface
    private interface ResultSetExtractor<T> {

        T extractData(ResultSet resultSet) throws SQLException;
    }
}
