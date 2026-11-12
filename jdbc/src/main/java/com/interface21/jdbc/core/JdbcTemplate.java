package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    /**
     * 호출하는 쪽이 넘긴 {@link Connection}으로 SQL을 실행한다.
     * <p>
     * 여러 SQL을 하나의 트랜잭션으로 묶을 때 사용한다. 커넥션의 생명주기와 커밋·롤백은 호출하는 쪽이 소유하므로
     * 이 메서드는 커넥션을 닫지 않는다.
     */
    public int update(final Connection connection, final String sql, final Object... args) {
        return execute(connection, sql, args, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, mapRows(rowMapper));
    }

    /**
     * 호출하는 쪽이 넘긴 {@link Connection}으로 조회한다. 커넥션은 닫지 않는다.
     */
    public <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                             final Object... args) {
        return execute(connection, sql, args, mapRows(rowMapper));
    }

    /**
     * 최대 한 건을 조회한다.
     * <p>
     * 조회 결과가 없으면 {@code null}을 반환하고, 두 건 이상이면 {@link DataAccessException}을 던진다.
     * 한 행이 존재하지만 {@link RowMapper}가 {@code null}을 반환한 경우도 {@code null}이므로,
     * "행 없음"과 구분할 필요가 있다면 {@link RowMapper}가 {@code null}을 반환하지 않도록 작성해야 한다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return singleResult(query(sql, rowMapper, args));
    }

    /**
     * 호출하는 쪽이 넘긴 {@link Connection}으로 최대 한 건을 조회한다. 커넥션은 닫지 않는다.
     * <p>
     * 결과 계약은 {@link #queryForObject(String, RowMapper, Object...)}와 같다.
     */
    public <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper,
                                final Object... args) {
        return singleResult(query(connection, sql, rowMapper, args));
    }

    private <T> T singleResult(final List<T> results) {
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, but found " + results.size());
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private <T> StatementExecutor<List<T>> mapRows(final RowMapper<T> rowMapper) {
        return statement -> {
            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        };
    }

    private <T> T execute(final String sql, final Object[] args, final StatementExecutor<T> executor) {
        try (final var connection = dataSource.getConnection()) {
            return execute(connection, sql, args, executor);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute sql: " + sql, e);
        }
    }

    private <T> T execute(final Connection connection, final String sql, final Object[] args,
                          final StatementExecutor<T> executor) {
        log.debug("query : {}", sql);
        try (final var statement = connection.prepareStatement(sql)) {
            bindParameters(statement, args);
            return executor.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute sql: " + sql, e);
        }
    }

    private void bindParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface StatementExecutor<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
