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
import java.util.Objects;

/**
 * SQL 실행과 조회 결과 변환을 처리한다.
 *
 * <p>{@link Connection}을 받는 메서드는 연결을 새로 얻거나 닫지 않는다.
 * 전달한 연결의 커밋, 롤백과 종료는 호출한 곳에서 처리한다.
 */
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    /**
     * SQL을 실행하고 영향을 받은 행 수를 반환한다.
     *
     * <p>SQL 값 하나가 {@code null}이면 타입을 {@code Object}로 지정한다.
     * {@code update(sql, null)}은 setter를 받는 메서드와 구분할 수 없어 컴파일되지 않는다.
     * <pre>{@code
     * jdbcTemplate.update("update users set email = ?", (Object) null);
     * }</pre>
     *
     * @param sql 실행할 SQL
     * @param args SQL 매개변수에 순서대로 넣을 값. 각 값은 {@code null}일 수 있지만 배열 자체는 {@code null}일 수 없다.
     * @return 영향을 받은 행 수
     * @throws NullPointerException args 배열 자체가 {@code null}인 경우
     */
    public int update(final String sql, final Object... args) {
        return update(sql, createPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public int update(final Connection connection, final String sql, final Object... args) {
        return update(connection, sql, createPreparedStatementSetter(args));
    }

    public int update(final Connection connection, final String sql, final PreparedStatementSetter setter) {
        return execute(connection, sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, createPreparedStatementSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        Objects.requireNonNull(rowMapper, "rowMapper must not be null");
        return execute(sql, setter, statement -> executeQuery(statement, rowMapper));
    }

    public <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                           final Object... args) {
        return query(connection, sql, rowMapper, createPreparedStatementSetter(args));
    }

    public <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                           final PreparedStatementSetter setter) {
        Objects.requireNonNull(rowMapper, "rowMapper must not be null");
        return execute(connection, sql, setter, statement -> executeQuery(statement, rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, createPreparedStatementSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return singleResult(query(sql, rowMapper, setter));
    }

    public <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                                final Object... args) {
        return queryForObject(connection, sql, rowMapper, createPreparedStatementSetter(args));
    }

    public <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                                final PreparedStatementSetter setter) {
        return singleResult(query(connection, sql, rowMapper, setter));
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter, final StatementCallback<T> callback) {
        Objects.requireNonNull(setter, "setter must not be null");
        try (Connection connection = dataSource.getConnection()) {
            return execute(connection, sql, setter, callback);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final Connection connection, final String sql, final PreparedStatementSetter setter,
                          final StatementCallback<T> callback) {
        Objects.requireNonNull(connection, "connection must not be null");
        Objects.requireNonNull(setter, "setter must not be null");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setter.setValues(statement);
            return callback.doInStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(final PreparedStatement statement, final RowMapper<T> rowMapper)
            throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return mapRows(resultSet, rowMapper);
        }
    }

    private <T> T singleResult(final List<T> results) {
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most 1 result, but found " + results.size());
        }
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    private <T> List<T> mapRows(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... args) {
        Objects.requireNonNull(args, "args must not be null");
        return statement -> {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        };
    }

    private interface StatementCallback<T> {

        T doInStatement(PreparedStatement statement) throws SQLException;
    }
}
