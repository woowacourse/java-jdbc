package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final StatementCreator statementCreator;
    private final StatementExecutor statementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this(dataSource, new StatementCreator(), new StatementExecutor());
    }

    JdbcTemplate(
            final DataSource dataSource,
            final StatementCreator statementCreator,
            final StatementExecutor statementExecutor
    ) {
        this.dataSource = dataSource;
        this.statementCreator = statementCreator;
        this.statementExecutor = statementExecutor;
    }

    private <T> T query(
            final String sql,
            final PreparedStatementCallback<T> preparedStatementCallback,
            final Object... parameters
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = statementCreator.create(connection, sql, parameters)) {
            return preparedStatementCallback.execute(preparedStatement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void update(final String sql, final Object... parameters) {
        query(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> results = query(sql, statement -> statementExecutor.execute(statement, rowMapper), parameters);
        if (results.size() > 1) {
            throw new DataAccessException("2개 이상의 결과를 반환할 수 없습니다.");
        }
        return results.stream().findAny();
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, statement -> statementExecutor.execute(statement, rowMapper), parameters);
    }
}
