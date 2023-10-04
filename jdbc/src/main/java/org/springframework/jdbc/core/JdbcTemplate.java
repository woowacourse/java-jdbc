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

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... conditions) {
        return getResult(PreparedStatement::executeUpdate, sql, conditions);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... conditions) {
        return Optional.ofNullable(getResult(preparedStatement -> getRowByQuery(preparedStatement, rowMapper), sql, conditions));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return singletonList(getResult(preparedStatement -> getRowByQuery(preparedStatement, rowMapper), sql));
    }

    private <T> T getRowByQuery(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) {
        try (
                final ResultSet resultSet = preparedStatement.executeQuery();
        ) {
            return new ResultSetProvider<>(rowMapper).getResults(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T getResult(final PreparedStatementExecutor<T> executor, final String sql, final Object... conditions) {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            setConditions(preparedStatement, conditions);

            return executor.query(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setConditions(PreparedStatement preparedStatement, Object[] conditions) throws SQLException {
        for (int i = 1; i <= conditions.length; i++) {
            preparedStatement.setObject(i, conditions[i - 1]);
        }
    }
}
