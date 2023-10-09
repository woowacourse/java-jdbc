package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... arguments) {
        return execute(sql, preparedStatement -> {
            setArguments(preparedStatement, arguments);
            return preparedStatement.executeUpdate();
        });
    }

    public int update(Transaction transaction, String sql, Object... arguments) {
        return transaction.execute(sql, preparedStatement -> {
            setArguments(preparedStatement, arguments);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        return execute(sql, preparedStatement -> {
            setArguments(preparedStatement, arguments);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new DataAccessException("결과가 없습니다");
            }

            return rowMapper.map(resultSet);
        });
    }

    public <T> T queryForObject(Transaction transaction, String sql, RowMapper<T> rowMapper, Object... arguments) {
        return transaction.execute(sql, preparedStatement -> {
            setArguments(preparedStatement, arguments);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new DataAccessException("결과가 없습니다");
            }

            return rowMapper.map(resultSet);
        });
    }

    public <T> List<T> queryForObjects(String sql, RowMapper<T> rowMapper, Object... arguments) {
        return execute(sql, preparedStatement -> {
            setArguments(preparedStatement, arguments);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            return results;
        });
    }

    public <T> List<T> queryForObjects(Transaction transaction, String sql, RowMapper<T> rowMapper, Object... arguments) {
        return transaction.execute(sql, preparedStatement -> {
            setArguments(preparedStatement, arguments);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            return results;
        });
    }

    private void setArguments(PreparedStatement preparedStatement, Object[] arguments) throws SQLException {
        for (int i = 0; i < arguments.length; i++) {
            preparedStatement.setObject(i + 1, arguments[i]);
        }
    }

    private <V> V execute(String sql, PreparedStatementExecutor<V> preparedStatementExecutor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
