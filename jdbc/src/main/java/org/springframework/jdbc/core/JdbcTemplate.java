package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.jdbc.core.PreparedStatementUtil.getPreparedStatement;
import static org.springframework.jdbc.core.PreparedStatementUtil.getResultSet;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer update(final String sql, final Object... conditions) {
        return getResult(PreparedStatement::executeUpdate, sql, conditions);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... conditions) {
        return Optional.of(getResult(preparedStatement -> getRowByQuery(preparedStatement, rowMapper), sql, conditions));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return singletonList(getResult(preparedStatement -> getRowByQuery(preparedStatement, rowMapper), sql, null));
    }

    private <T> T getRowByQuery(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) {
        final ResultSet resultSet = getResultSet(preparedStatement);
        return new ResultSetProvider<>(rowMapper).getResults(resultSet);
    }

    private <T> T getResult(final PreparedStatementExecutor<T> executor, final String sql, final Object... conditions) {
        final PreparedStatement preparedStatement = getPreparedStatement(getConnection(), sql, conditions);
        try {
            return executor.query(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
